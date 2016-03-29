package com.jerrylin.erp.query;

import java.io.Serializable;
import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.jerrylin.erp.component.SessionFactoryWrapper;
import com.jerrylin.erp.model.Member;
import com.jerrylin.erp.service.TimeService;
import com.jerrylin.erp.sql.From;
import com.jerrylin.erp.sql.ISqlNode;
import com.jerrylin.erp.sql.ISqlRoot;
import com.jerrylin.erp.sql.Join;
import com.jerrylin.erp.sql.OrderBy;
import com.jerrylin.erp.sql.Select;
import com.jerrylin.erp.sql.SqlRoot;
import com.jerrylin.erp.sql.SqlTarget;
import com.jerrylin.erp.sql.Where;
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
public class ConditionalQuery<T> implements Serializable{
	private static final long serialVersionUID = 5747837071652296027L;
	private static final String ID_FIELD = "id";
	
	private Logger logger = Logger.getLogger(ConditionalQuery.class.getName());
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
	public List<T> executeQueryPageable(Session s){
		String alias = findFirstSqlTargetAlias();
		String id = getIdFieldName();
		String identifier = alias + "." + id;
		
		// retrieving copy root, not originally
		SqlRoot copyRoot = copyRootPrepared();
		addOrderByIdIfAnyNotExisted(copyRoot);
		
		String selectSql = copyRoot.findSql(Select.class);
		String fromSql = copyRoot.findSql(From.class);
		String joinSql = copyRoot.findSql(Join.class);
		String whereSql = copyRoot.findSql(Where.class);
		String orderSql = copyRoot.findSql(OrderBy.class);
		
		if(joinSql.toLowerCase().contains("join fetch")){
			throw new UnsupportedOperationException("ConditionalQuery.executeQueryPageable NOT SUPPORT join fetch synctax!!");
		}
		
		String selectCount = "SELECT COUNT(DISTINCT " + identifier + ")";
		String selectCountHql = addLineBreakIfNotBlank(selectCount, fromSql, joinSql, whereSql);
		logger.log(Level.INFO, "selectCountHql: " + selectCountHql + "\n");
		
		String selectId = "SELECT DISTINCT " + identifier;
		String selectIdHql = addLineBreakIfNotBlank(selectCountHql, orderSql).replace(selectCount, selectId);
		logger.log(Level.INFO, "selectIdHql: " + selectIdHql + "\n");
		
		String selectAlias = "SELECT DISTINCT " + alias;
		String whereInIds = "WHERE " + identifier + " IN (:ids)";
		String whereInIdsHql = addLineBreakIfNotBlank(selectAlias, fromSql, whereInIds, orderSql);
		logger.log(Level.INFO, "whereInIdsHql: " + whereInIdsHql + "\n");
		
		Map<String, Object> params = copyRoot.getCondIdValuePairs();
		
		List<T> results = Collections.emptyList();
		int totalCount = 0;
		Iterator<Long> itr = s.createQuery(selectCountHql).setProperties(params).iterate();
		if(itr!=null && itr.hasNext()){
			totalCount = itr.next().intValue();
		}
		
		PageNavigator pn = new PageNavigator(totalCount, countPerPage);
		pn.setCurrentPage(currentPage);
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

	private static String addLineBreakIfNotBlank(String... statements){
		List<String> ori = Arrays.asList(statements);
		List<String> filtered = ori.stream().filter(s->StringUtils.isNotBlank(s)).collect(Collectors.toList());
		String append = StringUtils.join(filtered, "\n");
		return append;
	}
	
	public List<T> executeQueryList(Session s){
		SqlRoot copyRoot = copyRootPrepared();
		String queryHql = copyRoot.genSql();
		Map<String, Object> params = copyRoot.getCondIdValuePairs();
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
	 * this is the BASIC starting point of all OUTPUT methods on ConditionalQuery.
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
	
	public String findFirstSqlTargetAlias(){
		ISqlNode target = sqlRoot
			.find(n-> (n instanceof SqlTarget && n.getParent() instanceof From))
			.getFounds()
			.get(0);
		SqlTarget t = (SqlTarget)target;
		String alias = t.getAlias();
		return alias;
	}
	
	private String getSelectSql(SqlRoot root){
		ISqlNode target = root
			.find(n-> (n instanceof Select))
			.getFounds()
			.get(0);
		Select t = (Select)target;
		String sql = t.genSql();
		return sql;
	}
	
	private String getWhereSql(SqlRoot root){
		ISqlNode target = root
			.find(n-> (n instanceof Where))
			.getFounds()
			.get(0);
		Where t = (Where)target;
		String sql = t.genSql();
		return sql;
	}	
	
	private void addOrderByIdIfAnyNotExisted(SqlRoot root){
		List<ISqlNode> founds = root
				.find(n-> (n instanceof OrderBy))
				.getFounds();
		OrderBy node = null;
		if(founds.isEmpty()){
			node = root.orderBy();
		}else{
			node = (OrderBy)founds.get(0);
		}
		if(node.getChildren().isEmpty()){
			String alias = findFirstSqlTargetAlias();
			String id = getIdFieldName();
			String identifier = alias + "." + id;
			node.desc(identifier);
		}
	}
	
	private static <T>void testConditionalQuery(Consumer<ConditionalQuery<T>> consumer){
		BaseTest.executeApplicationContext(acac->{
			ConditionalQuery<T> q = acac.getBean(ConditionalQuery.class);
			consumer.accept(q);
		});
	}
	
	private static <T>void testExecuteQueryPageable(){
		testConditionalQuery(c->{
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

	private static <T>void testExecuteQueryPageableOrderBy(){
		testConditionalQuery(c->{
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
	public static void main(String[]args){
		testExecuteQueryPageableOrderBy();
	}
}
