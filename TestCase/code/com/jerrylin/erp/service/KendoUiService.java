package com.jerrylin.erp.service;

import static com.jerrylin.erp.service.TimeService.DF_yyyyMMdd_DASHED;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UnknownFormatConversionException;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.jerrylin.erp.component.ConditionConfig;
import com.jerrylin.erp.component.SessionFactoryWrapper;
import com.jerrylin.erp.model.Member;
import com.jerrylin.erp.query.ExecutableQuery;
import com.jerrylin.erp.sql.ISqlNode;
import com.jerrylin.erp.sql.ISqlRoot;
import com.jerrylin.erp.sql.OrderBy;
import com.jerrylin.erp.sql.SqlRoot;
import com.jerrylin.erp.sql.SqlTarget;
import com.jerrylin.erp.sql.Where;
import com.jerrylin.erp.sql.condition.CollectConds;
import com.jerrylin.erp.sql.condition.ISqlCondition;
import com.jerrylin.erp.sql.condition.SimpleCondition;
import com.jerrylin.erp.sql.condition.SqlCondition.Junction;
import com.jerrylin.erp.sql.condition.StrCondition.MatchMode;
import com.jerrylin.erp.test.BaseTest;
import com.jerrylin.erp.util.JsonParseUtil;

@Service
@Scope("prototype")
public class KendoUiService<T, R> implements Serializable{

	private static final long serialVersionUID = 5612145044684815434L;
	
	private static final String SIMPLE_CONDITION_PREFIX = "cond_";
	private static final String CURRENT_PAGE			= "currentPage";
	private static final String COUNT_PER_PAGE			= "countPerPage";
	private static final String ORDER_TYPE				= "orderType";
	private static final String KENDO_UI_GRID_FILTER	= "filter";
	private static final String KENDO_UI_FILTER_LOGIC_AND	= "and";
	private static final String KENDO_UI_FILTER_LOGIC_OR	= "or";
	private static final String KENDO_UI_DATA	= "kendoData";
	private static final String GROUP_AS_KENDO_UI_FILTER = "GROUP_AS_KENDO_UI_FILTER";
	
	private Logger logger = Logger.getLogger(KendoUiService.class.getName());
	
	@Autowired
	private ExecutableQuery<T> q;
	@Autowired
	private SessionFactoryWrapper sfw;
	@Autowired
	private ModelPropertyService modelPropertyService;
	
	private int filterCount;
	private String alias;
	private SqlTarget target;
	private Map<String, String> filterFieldConverter = Collections.emptyMap(); // 將前端回傳的field，轉成適當或預期的名字，讓hql可以正常執行；譬如member->member.name
	private Map<String, Class<?>> customDeclaredFieldTypes = Collections.emptyMap(); // 自定義field的型別，一般來說應該不需要
	
	public ConditionConfig<T> copyToConditionConfig(){
		ConditionConfig<T> cc = new ConditionConfig<T>();
		Map<String, Object> conds = cc.getConds();
		// simpleExpression
		getSqlRootImpl().findSimpleConditions()
		.forEach((s)->{
			conds.put(SIMPLE_CONDITION_PREFIX + s.getId(), s.getValue());
		});
		// paging
		conds.put(CURRENT_PAGE, q.getCurrentPage());
		conds.put(COUNT_PER_PAGE, q.getCountPerPage());
		if(q.getPageNavigator()!=null){
			cc.setPageNavigator(q.getPageNavigator());
		}
		// sorting
//		conds.put(ORDER_TYPE, null);
		return cc;
	}
	
	public void copyFromConditionConfig(ConditionConfig<T> conds){
		SqlRoot root = getSqlRootImpl();
		Map<String, Object> all = conds.getConds();
		SqlTarget target = getFirstSqlTarget();
		// predefined conditions populate value
		all.keySet().stream().filter(k->k.startsWith(SIMPLE_CONDITION_PREFIX)).forEach(k->{
			String id = k.replace(SIMPLE_CONDITION_PREFIX, "");
			List<ISqlNode> founds = root.findNodeById(id).getFounds();
			if(founds.size() == 1){
				SimpleCondition s = (SimpleCondition)founds.get(0);
				addValToSimpleCondition(s, all.get(k));
			}
		});
		
		Map<String, Object> kendoData = (Map<String, Object>)all.get(KENDO_UI_DATA);		
		if(kendoData != null){
			if(all.get("moduleName")!=null){
				String moduleName = (String)all.get("moduleName");
				HttpSession session = (((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest()).getSession();
				session.setAttribute(moduleName + "KendoData", JsonParseUtil.parseToJson(kendoData));
			}
			
			// kendo ui filter conditions
			Object filter = kendoData.get(KENDO_UI_GRID_FILTER);
			// remove kendo ui filter conditions
			root.find(n->(n instanceof ISqlCondition && (GROUP_AS_KENDO_UI_FILTER.equals(((ISqlCondition)n).getGroupMark())))).remove();
			if(null != filter){
				adjustConditionByKendoUIGridFilter(filter);
			}
			
			// paging configuration
			Integer currentPage = getInteger(kendoData.get("page"));
			Integer countPerPage = getInteger(kendoData.get("pageSize"));
			if(null != currentPage){
				q.setCurrentPage(currentPage);
			}
			if(null != countPerPage){
				q.setCountPerPage(countPerPage);
			}
		
			// order by configuration
			List<Map<String, String>> orderTypes = (List<Map<String, String>>)kendoData.get("sort");
			OrderBy orderBy = root.find(OrderBy.class);
			if(null == orderBy){
				orderBy = root.orderBy();
			}
			orderBy.getChildren().clear();
			if(null != orderTypes){
				String alias = getAlias();
				for(int i = 0; i < orderTypes.size(); i++){
					Map<String, String> orderType = orderTypes.get(i); // Kendo UI Grid排序回傳的資料結構 
					String field = orderType.get("field");
					field = convertFilterField(field);
					String dir = orderType.get("dir");
					if("asc".equals(dir)){
						orderBy.asc(alias + "." + field);
					}else if("desc".equals(dir)){
						orderBy.desc(alias + "." + field);
					}
				}
			}
			
		}
	}
	
	@Transactional
	public List<T> batchSaveOrMerge(List<T> targets){
		Session s = sfw.getCurrentSession();
		int batchSize = sfw.getBatchSize();
		int count = 0;
		
		for(int i = 0; i < targets.size(); i++){
			T target = targets.get(i);
			String pk = null;
			try{
				Object propVal = PropertyUtils.getProperty(target, q.getIdFieldName());
				if(null != propVal && propVal.getClass() == String.class){
					pk = (String)propVal;
				}
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
			if(StringUtils.isBlank(pk)){
				s.save(target);
			}else{
				s.update(target);
			}
			if(++count % batchSize == 0){
				s.flush();
				s.clear();
			}
		}
		s.flush();
		s.clear();
		
		return targets;
	}
	
	@Transactional
	public String deleteByIds(List<String> ids){
		Session s = sfw.getCurrentSession();
		String queryHql = "SELECT DISTINCT p FROM " + q.findFirstSqlTarget().getTargetClass().getName() + " p WHERE p."+ q.getIdFieldName() +" IN (:ids)";
		ScrollableResults results = s.createQuery(queryHql).setParameterList("ids", ids).scroll(ScrollMode.FORWARD_ONLY);
		while(results.next()){
			Object target = results.get()[0];
			s.delete(target);
		}
		s.flush();
		s.clear();
		
		return "";
	}
	
	private void adjustConditionByKendoUIGridFilter(Object filterObj){		
		SqlRoot root = getSqlRootImpl();
		Where where = root.find(Where.class);
		if(null == where){
			where = root.where();
		}
		
		CollectConds conds = null;
		List<ISqlNode> children = where.getChildren();
		if(!children.isEmpty()){
			ISqlNode lastChild = children.get(children.size()-1);
			CollectConds last = (CollectConds)lastChild;
			if(last.getJunction() == Junction.AND){
				conds = last;
			}
		}
		
		if(conds == null){
			conds = where.andConds();
		}
		
		Map<String, Object> filter = (Map<String, Object>)filterObj;
		String logic = (String)filter.get("logic");
		List<Map<String, Object>> filters = (List<Map<String, Object>>)filter.get("filters");
		
		filterCount = 0;
		addFilterCondtions(filters, conds, logic);
	}
	
	private void addFilterCondtions(List<Map<String, Object>> filters, CollectConds parent, String ParentLogic){
		parent.enableGroupMark(GROUP_AS_KENDO_UI_FILTER);
		for(int i = 0; i < filters.size(); i++){
			Map<String, Object> filter = filters.get(i);
			String logic = (String)filter.get("logic");
			if(StringUtils.isNotBlank(logic)){
				List<Map<String, Object>> f= (List<Map<String, Object>>)filter.get("filters");
				addFilterCondtions(f, parent.andCollectConds(), logic);
			}else{
				addFilterCondition(filter, parent, ParentLogic);
			}
		}
		parent.disableGroupMark();
	}
	
	private void addFilterCondition(Map<String, Object> f, CollectConds conds, String logic){
		String operator = (String)f.get("operator");
		String field = (String)f.get("field");
		field = convertFilterField(field);
		Object value = f.get("value");
		
		filterCount++;
		
		String alias = getAlias();
		String expression = alias + "." + field + " ";
		String nameParam = " :" + alias + firstLetterToUpperCase(field) + "_FILTER_" + filterCount;
		MatchMode matchMode = null;
		Object convertedVal = convertValueByType(field, value);
		switch(operator){
			case "isnull":
				expression += "IS NULL";
				addStatement(conds, expression, logic);
				break;
			case "isnotnull":
				expression += "IS NOT NULL";
				addStatement(conds, expression, logic);
				break;
			case "isempty":
				expression += "IS EMPTY";
				addStatement(conds, expression, logic);
				break;
			case "isnotempty":
				expression += "IS NOT EMPTY";
				addStatement(conds, expression, logic);
				break;
				
			case "eq":
				expression += ("=" + nameParam);
				addSimpleCond(conds, expression, logic, value, convertedVal);
				break;						
			case "neq":
				expression += ("!=" + nameParam);
				addSimpleCond(conds, expression, logic, value, convertedVal);
				break;						
			case "gte":
				expression += (">=" + nameParam);
				addSimpleCond(conds, expression, logic, value, convertedVal);
				break;						
			case "gt":
				expression += (">" + nameParam);
				addSimpleCond(conds, expression, logic, value, convertedVal);
				break;						
			case "lte":
				expression += ("<=" + nameParam);
				addSimpleCond(conds, expression, logic, value, convertedVal);
				break;						
			case "lt":
				expression += ("<" + nameParam);
				addSimpleCond(conds, expression, logic, value, convertedVal);
				break;
				
			case "startswith":
				matchMode = MatchMode.START;
				expression += ("LIKE" + nameParam);
				addStrCond(conds, expression, value, matchMode, logic);
				break;						
			case "endswith":
				matchMode = MatchMode.END;
				expression += ("LIKE" + nameParam);
				addStrCond(conds, expression, value, matchMode, logic);						
				break;						
			case "contains":
				matchMode = MatchMode.ANYWHERE;
				expression += ("LIKE" + nameParam);
				addStrCond(conds, expression, value, matchMode, logic);						
				break;
			case "doesnotcontain":
				matchMode = MatchMode.ANYWHERE;
				expression += ("NOT LIKE" + nameParam);
				addStrCond(conds, expression, value, matchMode, logic);
				break;
		}
	}
	// 這裡直接加入不管大小寫的邏輯
	private void addStrCond(CollectConds conds, String expression, Object value, MatchMode matchMode, String logic){
		if("and".equals(logic)){
			conds.andStrToUpperCase(expression, matchMode, (String)value);
		}else{
			conds.orStrToUpperCase(expression, matchMode, (String)value);
		}
	}
	
	private void addStatement(CollectConds conds, String expression, String logic){
		if("and".equals(logic)){
			conds.andStatement(expression);
		}else{
			conds.orStatement(expression);
		}
	}
	
	private void addSimpleCond(CollectConds conds, String expression, String logic, Object value, Object convertedVal){
		if("and".equals(logic)){
			conds.andSimpleCond(expression, value.getClass(), convertedVal);
		}else{
			conds.orSimpleCond(expression, value.getClass(), convertedVal);
		}
	}
	
	/**
	 * 必要時轉換Kendo UI所提供的fieldName，如果沒有轉換就輸出原來的值
	 * @param fieldName
	 * @return
	 */
	private String convertFilterField(String fieldName){
		if(filterFieldConverter.containsKey(fieldName)){
			return filterFieldConverter.get(fieldName);
		}
		return fieldName;
	}
	
	private Integer getInteger(Object val){
		if(val == null){
			return null;
		}
		if(val instanceof String){
			return Integer.parseInt((String)val);
		}
		if(val instanceof Integer){
			return (Integer)val;
		}
		return null;
	}
	
	public ConditionConfig<T> executeQueryPageable(ConditionConfig<T> conditionConfig){
		if(conditionConfig != null){
			copyFromConditionConfig(conditionConfig);
		}
		return genCondtitionsAfterExecuteQueryPageable();
	}
	
	public ConditionConfig<T> genCondtitionsAfterExecuteQueryPageable(){
		List<T> results = q.executeQueryPageable();
		ConditionConfig<T> cc = copyToConditionConfig();
		cc.setResults(results);
		return cc;
	}
	
	public ISqlRoot getSqlRoot(){
		return q.getSqlRoot();
	}
	
	private SqlRoot getSqlRootImpl(){
		return (SqlRoot)getSqlRoot();
	}
	
	public void setFilterFieldConverter(Map<String, String> filterFieldConverter){
		this.filterFieldConverter = filterFieldConverter;
	}
	
	public void setCustomDeclaredFieldTypes(Map<String, Class<?>> customDeclaredFieldTypes){
		this.customDeclaredFieldTypes = customDeclaredFieldTypes;
	}
	
	private void addValToSimpleCondition(SimpleCondition s, Object obj){
		if(obj == null){
			return;
		}
//		Class<?> type = s.getType();
		String field = s.getPropertyName().replace(getFirstSqlTarget().getAlias() + ".", "");
		Object casted = convertValueByType(field, obj);
		s.value(casted);
	}
	
	private Object convertValueByType(String field, Object obj){
		Class<?> type = this.customDeclaredFieldTypes.get(field);
		if(type == null){
			type = this.modelPropertyService.getModelPropertyTypes().get(getFirstSqlTarget().getTargetClass()).get(field);
		}
		
		Object casted = transformByType(type, obj);
		return casted;
	}
	
	private static Object transformByType(Class<?> type, Object obj){
		if(null == obj || obj.getClass() == type){
			return obj;
		}
		Object casted = null;
		try{
			if(obj instanceof String){
				String val = (String)obj;
				if(StringUtils.isBlank(val)){
					return casted;
				}
				val = val.trim();
				if(type == String.class){
					casted = val;
				}else if(type == Boolean.class){
					casted = Boolean.parseBoolean(val);
				}else if(type == Integer.class){
					casted = Integer.parseInt(val);
				}else if(type == Double.class){
					casted = Double.parseDouble(val);
				}else if(type == Float.class){
					casted = Float.parseFloat(val);
				}else if(type == Date.class){
					casted = new Date(DF_yyyyMMdd_DASHED.parse(val).getTime());
				}else if(type == Timestamp.class){
					casted = new Timestamp(DF_yyyyMMdd_DASHED.parse(val).getTime());
				}else{
					throw new UnknownFormatConversionException("type: " + type + " NOT defined yet");
				}
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
//		System.out.println("KendoUiService.transformByType type: " + type + "obj: " + obj + ", casted: " + casted);
		return casted;
	}
	
	private SqlTarget getFirstSqlTarget(){
		if(target == null){
			target = q.findFirstSqlTarget();
		}
		return target;
	}
	
	private String getAlias(){
		if(StringUtils.isBlank(alias)){
			alias = q.findFirstSqlTargetAlias();
		}
		return alias;
	}
	
	private static Integer parseInteger(Object obj){
		Integer i = null;
		if(obj == null){
			return i;
		}
		if(obj instanceof String && NumberUtils.isNumber((String)obj)){
			i = Integer.parseInt((String)obj);
		}else if(obj instanceof Integer){
			i = (Integer)obj;
		}
		return i;
	}
	
	private static String firstLetterToUpperCase(String input){
		String first = input.substring(0, 1);
		String firstToUpper = first.toUpperCase();
		String firstRemoved = StringUtils.removeStart(input, first);
		String result = firstToUpper + firstRemoved;
		
		return result;
	}
	
	private static void testBaseOperation(){
		BaseTest.executeApplicationContext(acac->{
			KendoUiService<Member, Member> q = acac.getBean(KendoUiService.class);
			q.getSqlRoot()
			.select()
				.target("p").getRoot()
			.from()
				.target(Member.class.getName(), "p").getRoot()
			.where()
				.andConds()
					.andSimpleCond("p.name = :pName", String.class)
					.andSimpleCond("p.idNo = :pIdNo", String.class)
					.andSimpleCond("p.mobile = :pMobile", String.class);
			ConditionConfig<Member> c = q.genCondtitionsAfterExecuteQueryPageable();
			c.getResults().forEach(m->{
				System.out.println(m.getId());
			});
		});
	}
	
	private static void testSwitch(String operator){
		String expression = "p.name = ";
		switch(operator){
		case "isnull":
			expression += "IS NULL";
		case "isnotnull":
			expression += "IS NOT NULL";				
		case "isempty":
			expression += "IS EMPTY";
		case "isnotempty":
			expression += "IS NOT EMPTY";
			break;
		}
		System.out.println(expression);
	}
	
	private static void testFirstLetterToUpperCase(){
		String t1 = "name";
		String result = firstLetterToUpperCase(t1);
		System.out.println(result);
	}
	
	private static void testTransformByType(){
		System.out.println(transformByType(Date.class, "2016-03-01T16:00:00.000Z"));
	}
	
	public static void main(String[]args){

	}
}
