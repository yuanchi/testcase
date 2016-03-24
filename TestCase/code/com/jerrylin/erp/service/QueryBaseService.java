package com.jerrylin.erp.service;

import static com.jerrylin.erp.service.TimeService.DF_yyyyMMdd_DASHED;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UnknownFormatConversionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.jerrylin.erp.component.ConditionConfig;
import com.jerrylin.erp.model.Member;
import com.jerrylin.erp.query.ConditionalQuery;
import com.jerrylin.erp.sql.ISqlNode;
import com.jerrylin.erp.sql.ISqlRoot;
import com.jerrylin.erp.sql.SqlRoot;
import com.jerrylin.erp.sql.condition.SimpleCondition;
import com.jerrylin.erp.test.BaseTest;

@Service
@Scope("prototype")
public class QueryBaseService<T, R> {
	private static final String SIMPLE_CONDITION_PREFIX = "cond_";
	private static final String CURRENT_PAGE			= "currentPage";
	private static final String COUNT_PER_PAGE			= "countPerPage";
	private static final String ORDER_TYPE				= "orderType";
	@Autowired
	private ConditionalQuery<T> q;
	
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
		conds.put(ORDER_TYPE, null);
		return cc;
	}
	
	public void copyFromConditionConfig(ConditionConfig<T> conds){
		SqlRoot root = getSqlRootImpl();
		Map<String, Object> all = conds.getConds();
		all.keySet().stream().filter(k->k.startsWith(SIMPLE_CONDITION_PREFIX)).forEach(k->{
			String id = k.replace(SIMPLE_CONDITION_PREFIX, "");
			List<ISqlNode> founds = root.findNodeById(id).getFounds();
			if(founds.size() == 1){
				SimpleCondition s = (SimpleCondition)founds.get(0);
				addValToSimpleCondition(s, all.get(k));
			}
		});
		q.setCurrentPage(parseInteger(all.get(CURRENT_PAGE)));
		q.setCountPerPage(parseInteger(all.get(COUNT_PER_PAGE)));
		
		String orderBy = (String)all.get(ORDER_TYPE);
		if(StringUtils.isNotBlank(orderBy)){
			// TODO
		}
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
	
	private void addValToSimpleCondition(SimpleCondition s, Object obj){
		if(obj == null){
			return;
		}
		Class<?> type = s.getType();
		Object casted = transformByType(type, obj);
		s.value(casted);
	}
	
	private static Object transformByType(Class<?> type, Object obj){
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
		return casted;
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
	
	private static void testBaseOperation(){
		BaseTest.executeApplicationContext(acac->{
			QueryBaseService<Member, Member> q = acac.getBean(QueryBaseService.class);
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
	
	public static void main(String[]args){
		testBaseOperation();
	}
}
