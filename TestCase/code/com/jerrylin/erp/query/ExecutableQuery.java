package com.jerrylin.erp.query;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jerrylin.erp.component.SessionFactoryWrapper;
import com.jerrylin.erp.model.Member;
import com.jerrylin.erp.service.TimeService;
import com.jerrylin.erp.sql.From;
import com.jerrylin.erp.sql.ISqlNode;
import com.jerrylin.erp.sql.ISqlRoot;
import com.jerrylin.erp.sql.Join;
import com.jerrylin.erp.sql.OrderBy;
import com.jerrylin.erp.sql.SqlRoot;
import com.jerrylin.erp.sql.SqlTarget;
import com.jerrylin.erp.sql.Where;
import com.jerrylin.erp.sql.condition.StrCondition.MatchMode;
import com.jerrylin.erp.test.BaseTest;

@Service
@Scope("prototype")
/**
 * basic implementation of querying by condition, including common and paging query
 * required: configure sqlRoot
 * required: confirm (and configure if needed) primary key name; default is 'id', representing mapping entity has a field name 'id' as primary key 
 * @author JerryLin
 *
 * @param <T>
 */
public class ExecutableQuery<T> implements Serializable{
	private static final long serialVersionUID = 5747837071652296027L;
	private static final String ID_FIELD = "id";
	
	private Logger logger = Logger.getLogger(ExecutableQuery.class.getName());
	private String idFieldName = ID_FIELD;
	
	@Autowired
	private SessionFactoryWrapper sfw;
	@Autowired
	private SqlRoot sqlRoot;
	@Autowired
	private TimeService timeService;
	
	private int currentPage = 1;
	private int countPerPage = 10;
	private PageNavigator pageNavigator;
	
	public SqlRoot getSqlRoot(){
		return sqlRoot;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public int getCountPerPage() {
		return countPerPage;
	}
	public void setCountPerPage(int countPerPage) {
		this.countPerPage = countPerPage;
	}
	public PageNavigator getPageNavigator() {
		return pageNavigator;
	}
	public String getIdFieldName(){
		return idFieldName;
	}
	public void setIdFieldName(String idFieldName){
		this.idFieldName = idFieldName;
	}
	
	public List<T> executeQueryPageable(){
		List<T> results = sfw.executeFindResults(s->{
			return executeQueryPageable(s);
		});
		return results;
	}
	/**
	 * paging query
	 * attention: NOT use 'JOIN fetch' syntax here
	 * @param s
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<T> executeQueryPageable(Session s){
		SqlGenerator genSql = new SqlGenerator(this);
		
		String alias = genSql.alias;
		String identifier = genSql.identifier;

		String fromSql = genSql.from;
		String joinSql = genSql.join;
		String whereSql = genSql.where;
		String orderSql = genSql.order;
		Map<String, Object> params = genSql.params;
		
		String selectCount = "SELECT COUNT(DISTINCT " + identifier + ")";
		String selectCountHql = addLineBreakIfNotBlank(selectCount, fromSql, joinSql, whereSql);
		logger.log(Level.INFO, "selectCountHql: " + selectCountHql + "\n");
		System.out.println("selectCountHql: " + selectCountHql + "\n");
		
		String selectId = "SELECT DISTINCT " + identifier;
		String selectIdHql = addLineBreakIfNotBlank(selectCountHql, orderSql).replace(selectCount, selectId);
//		logger.log(Level.INFO, "selectIdHql: " + selectIdHql + "\n");
		System.out.println("selectIdHql: " + selectIdHql + "\n");
		
		String selectAlias = "SELECT DISTINCT " + alias;
		String whereInIds = "WHERE " + identifier + " IN (:ids)";
		String whereInIdsHql = addLineBreakIfNotBlank(selectAlias, fromSql, joinSql, whereInIds, orderSql);
//		logger.log(Level.INFO, "whereInIdsHql: " + whereInIdsHql + "\n");
		System.out.println("whereInIdsHql: " + whereInIdsHql + "\n");
		
//		logger.log(Level.INFO, "params:");
		System.out.println("params:");
		params.forEach((k,v)->{
//			logger.log(Level.INFO, k + ":" + v + ":" + v.getClass());
			System.out.println(k + ":" + v + ":" + v.getClass());
		});
		
		List<T> results = Collections.emptyList();
		int totalCount = 0;
		Iterator<Long> itr = s.createQuery(selectCountHql).setProperties(params).iterate();
		if(itr!=null && itr.hasNext()){
			totalCount = itr.next().intValue();
		}
		
		PageNavigator pn = new PageNavigator(totalCount, countPerPage);
		pn.setCurrentPage(currentPage);
		System.out.println("currentPage:"+currentPage);
		pageNavigator = pn;
		
		List<String> ids = s.createQuery(selectIdHql)
							.setProperties(params)
							.setMaxResults(pn.getCountPerPage())
							.setFirstResult(pn.countFirstResultIndex())
							.list();
		if(ids.isEmpty()){
			return results;
		}
		results = s.createQuery(whereInIdsHql).setParameterList("ids", ids).list();
		return results;
	}

	@Transactional
	public <F>F executeScrollableQuery(BiFunction<ScrollableResults, SessionFactoryWrapper, F> executeLogic){
		Session s = sfw.getCurrentSession();
		
		SqlGenerator genSql = new SqlGenerator(this);
		Map<String, Object> params = genSql.params;
		String selectAliasHql = genSql.selectDistinctAlias();
		
		ScrollableResults rs = s.createQuery(selectAliasHql).setProperties(params).scroll(ScrollMode.FORWARD_ONLY);
		F target = executeLogic.apply(rs, sfw);
		
		return target;
	}	
	
	private static class SqlGenerator{
		String alias;
		String id;
		String identifier;
		SqlRoot copyRoot;
		String from;
		String join;
		String where;
		String order;
		Map<String, Object> params;
		SqlGenerator(ExecutableQuery<?> q){
			this.alias = q.findFirstSqlTargetAlias();
			this.id = q.getIdFieldName();
			this.identifier = alias + "." + id;
			this.copyRoot = q.copyRootPrepared();
			this.from = copyRoot.findSql(From.class);
			this.join = copyRoot.findSql(Join.class);
			this.where = copyRoot.findSql(Where.class);
			this.order = copyRoot.findSql(OrderBy.class);
			this.params = copyRoot.getCondIdValuePairs();
			
			if(join.toLowerCase().contains("join fetch")){
				throw new UnsupportedOperationException("ExecutableQuery.executeQueryPageable NOT SUPPORT join fetch synctax!!");
			}
			
			String defaultDirection = " DESC";
			if(StringUtils.isBlank(order)){
				order = "ORDER BY " + identifier + defaultDirection;
			}
			if(!order.contains(identifier + " ")){
				order += ", " + identifier + defaultDirection;
			}
		}
		
		String selectDistinctAlias(){
			String selectAlias = "SELECT DISTINCT " + alias;
			String selectDistinctAlias = addLineBreakIfNotBlank(selectAlias, from, join, where, order);
			return selectDistinctAlias;
		}
		
	}
	
	private static String addLineBreakIfNotBlank(String... statements){
		List<String> ori = Arrays.asList(statements);
		List<String> filtered = ori.stream().filter(s->StringUtils.isNotBlank(s)).collect(Collectors.toList());
		String append = StringUtils.join(filtered, "\n");
		return append;
	}
	
	@SuppressWarnings("unchecked")
	public List<T> executeQueryList(Session s){
		SqlGenerator genSql = new SqlGenerator(this);
		Map<String, Object> params = genSql.params;
		String queryHql = genSql.selectDistinctAlias();
		
		List<T> results = s.createQuery(queryHql).setProperties(params).list();
		return results;
	}
	
	public List<T> executeQueryList(){
		List<T> r = sfw.executeFindResults(s->{
			List<T> results = executeQueryList(s);
			return results;
		});
		return r;
	}
	/**
	 * this is the BASIC starting point of all OUTPUT methods on ExecutableQuery.
	 * first exclude nodes not required and copy,
	 * secondly transform nodes meeting the criteria and copy,
	 * lastly get copy root node
	 * @return
	 */
	SqlRoot copyRootPrepared(){
		ISqlRoot copy1 = sqlRoot.excludeCopy(excludeNode());
		ISqlRoot copy2 = copy1.transformCopy(transformNode());
		return (SqlRoot)copy2;
	}
	/**
	 * provide exclude node predicate
	 * default use SqlRoot built-in excludeNode
	 * @return
	 */
	Predicate<ISqlNode> excludeNode(){
		return sqlRoot.excludeNode();
	}
	/**
	 * provide transform node consumer
	 * default use SqlRoot built-in transformNode
	 * @return
	 */
	Consumer<ISqlNode> transformNode(){
		return sqlRoot.transformNode();
	}
	
	public SqlTarget findFirstSqlTarget(){
		ISqlNode target = sqlRoot
				.find(n-> (n instanceof SqlTarget && n.getParent() instanceof From))
				.getFounds()
				.get(0);
			SqlTarget t = (SqlTarget)target;
		return t;
	}
	
	public String findFirstSqlTargetAlias(){
		String alias = findFirstSqlTarget().getAlias();
		return alias;
	}
	
	@SuppressWarnings("unchecked")
	private static <T>void testExecutableQuery(Consumer<ExecutableQuery<T>> consumer){
		BaseTest.executeApplicationContext(acac->{
			ExecutableQuery<T> q = acac.getBean(ExecutableQuery.class);
			consumer.accept(q);
		});
	}
	
	@SuppressWarnings("unused")
	private static <T>void testExecuteQueryPageable(){
		testExecutableQuery(c->{
			SqlRoot root = c.getSqlRoot();
			root.select()
					.target("p", "member").getRoot()
				.from()
					.target(Member.class.getName(), "p").getRoot()
				.joinAlias("LEFT JOIN p.vipDiscountDetails", "vipDetails")
				.where()
					.andConds()
						.andSimpleCond("p.idNo LIKE :pIdNo", String.class, "P%")
						.andSimpleCond("vipDetails.effectiveEnd > :vEffectiveEnd", Date.class, c.timeService.atStartOfToday())
						.andSimpleCond("p.name LIKE :pName", String.class).getRoot()
				.orderBy()
					.desc("p.idNo");
			
			List<Object> r = c.executeQueryList();
			r.forEach(o->{
				Member m = (Member)o;
				System.out.println(m.getId());
			});
			
			List<Object> results = c.executeQueryPageable();
			results.forEach(o->{
				Member m = (Member)o;
				System.out.println(m.getId() + "|" + m.getName());
//				List<VipDiscountDetail> details = m.getVipDiscountDetails();
//				System.out.println("details count: " + details.size());
			});
		});
	}

	@SuppressWarnings("unused")
	private static <T>void testExecuteQueryPageableOrderBy(){
		testExecutableQuery(c->{
			SqlRoot root = c.getSqlRoot();
			root.select()
					.target("p", "member").getRoot()
				.from()
					.target(Member.class.getName(), "p").getRoot()
				.where()
					.andConds()
						.andSimpleCond("p.idNo LIKE :pIdNo", String.class)
						.andSimpleCond("p.name LIKE :pName", String.class).getRoot()
						;
			
			List<Object> r = c.executeQueryList();
			System.out.println("executeQueryList:");
			r.forEach(o->{
				Member m = (Member)o;
				System.out.println(m.getId());
			});
			
			List<Object> results = c.executeQueryPageable();
			System.out.println("executeQueryPageable:");
			results.forEach(o->{
				Member m = (Member)o;
				System.out.println(m.getId() + "|" + m.getName());
//				List<VipDiscountDetail> details = m.getVipDiscountDetails();
//				System.out.println("details count: " + details.size());
			});
		});
	}
	
	@SuppressWarnings("unused")
	private static void testConditionStatement(){
		testExecutableQuery(c->{
			SqlRoot root = c.getSqlRoot();
			root.select()
					.target("p", "member").getRoot()
				.from()
					.target(Member.class.getName(), "p").getRoot()
//				.where()
//					.andConds()
//						.andStatement("p.name IS NOT NULL").getRoot()
						;
			System.out.println(root.genSql());
			c.setCurrentPage(2);
			List<Object> ms = c.executeQueryPageable();
			ms.stream().forEach(m->{
				Member member = (Member)m;
				System.out.println(member.getId() + member.getName());
			});
		});
	}
	
	@SuppressWarnings("unused")
	private static void testContainLike(){
		testExecutableQuery(c->{
			SqlRoot root = c.getSqlRoot();
			root.select()
					.target("p", "member").getRoot()
				.from()
					.target(Member.class.getName(), "p").getRoot()
				.where()
					.andConds()
						.andStrCondition("p.name LIKE :pName", MatchMode.ANYWHERE, "竹").getRoot()
						;
			
			c.executeQueryPageable();
		});
	}
	
	@SuppressWarnings("unused")
	private static void testAddAfterRemove(){
		testExecutableQuery(c->{
			SqlRoot root = c.getSqlRoot();
			root.select()
					.target("p", "member").getRoot()
				.from()
					.target(Member.class.getName(), "p").getRoot()
				.where()
					.andConds()
						.andStrCondition("p.name LIKE :pName", MatchMode.ANYWHERE, "竹").getRoot()
						;
			
			c.executeQueryPageable();
		});
	}
	
	@SuppressWarnings("unused")
	private static void testSimpleConditionId(){
		testExecutableQuery(c->{
			SqlRoot root = c.getSqlRoot();
			root.select()
					.target("p", "member").getRoot()
				.from()
					.target(Member.class.getName(), "p").getRoot()
				.where()
					.andConds()
						.andStrCondition("p.name LIKE :pName_FILTER_1", MatchMode.ANYWHERE, "竹").getRoot()
						;
			System.out.println(root.genSql());
		});
	}
	
	@SuppressWarnings("unused")
	private static void testLocalDateTimeParse(){
		String time = "2016-03-01T16:00:00.000Z";
		
		LocalDateTime ldt = LocalDateTime.parse(time, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		System.out.println(ldt);
	}
	
	private static <T>void testClassFieldsInfo(Class<T> clz){
		Map<String, Class<?>> info = new HashMap<>();
		Field[] fields = clz.getDeclaredFields();
		for(Field field : fields){
			String name = field.getName();
			Class<?> type = field.getType();
			info.put(name, type);
			System.out.println(name +":"+type);
		}
	}
	
	public static void main(String[]args){
		testConditionStatement();
	}
}
