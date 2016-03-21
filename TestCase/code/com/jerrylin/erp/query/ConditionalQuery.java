package com.jerrylin.erp.query;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;







import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;







import com.jerrylin.erp.component.SessionFactoryWrapper;
import com.jerrylin.erp.model.Member;
import com.jerrylin.erp.sql.From;
import com.jerrylin.erp.sql.ISqlNode;
import com.jerrylin.erp.sql.OrderBy;
import com.jerrylin.erp.sql.Select;
import com.jerrylin.erp.sql.SqlRoot;
import com.jerrylin.erp.sql.SqlTarget;
import com.jerrylin.erp.sql.Where;
import com.jerrylin.erp.test.BaseTest;

@Service
@Scope("prototype")
public class ConditionalQuery<T> implements Serializable{
	private static final long serialVersionUID = 5747837071652296027L;
	private static final String ID_FIELD = "id";
	
	private Logger logger = Logger.getLogger(ConditionalQuery.class.getName());
	
	@Autowired
	private SessionFactoryWrapper sfw;
	@Autowired
	private SqlRoot sqlRoot;
	
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

	public List<T> executeQueryPageable(){
		List<T> results = sfw.executeSession(s->{
			return executeQueryPageable(s);
		});
		return results;
	}
	public List<T> executeQueryPageable(Session s){
		String alias = findFirstSqlTargetAlias();
		String identifier = alias + "." + ID_FIELD;
		String select = getSelectSql();
		String where = getWhereSql();
		addOrderByIdIfAnyNotExisted();
		String rootSql = sqlRoot.genSql();
		
		String selectCount = "SELECT COUNT(DISTINCT " + identifier + ")";
		String selectCountHql = rootSql.replace(select, selectCount);
		logger.log(Level.WARNING, "selectCountHql: " + selectCountHql + "\n");
		
		String selectId = "SELECT DISTINCT " + identifier;
		String selectIdHql = rootSql.replace(select, selectId);
		logger.log(Level.WARNING, "selectIdHql: " + selectIdHql + "\n");
		
		String selectAlias = "SELECT DISTINCT " + alias;
		String whereInIds = "WHERE " + identifier + " IN (:ids)";
		String whereInIdsHql = rootSql.replace(select, selectAlias)
									.replace(where, whereInIds);
		logger.log(Level.WARNING, "whereInIdsHql: " + whereInIdsHql + "\n");
		
		Map<String, Object> params = sqlRoot.getCondIdValuePairs();
		
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

	private String findFirstSqlTargetAlias(){
		ISqlNode target = sqlRoot
			.find(n-> (n instanceof SqlTarget && n.getParent() instanceof From))
			.getFounds()
			.get(0);
		SqlTarget t = (SqlTarget)target;
		String alias = t.getAlias();
		return alias;
	}
	
	private String getSelectSql(){
		ISqlNode target = sqlRoot
			.find(n-> (n instanceof Select))
			.getFounds()
			.get(0);
		Select t = (Select)target;
		String sql = t.genSql();
		return sql;
	}
	
	private String getWhereSql(){
		ISqlNode target = sqlRoot
			.find(n-> (n instanceof Where))
			.getFounds()
			.get(0);
		Where t = (Where)target;
		String sql = t.genSql();
		return sql;
	}	
	
	private void addOrderByIdIfAnyNotExisted(){
		List<ISqlNode> founds = sqlRoot
				.find(n-> (n instanceof OrderBy))
				.getFounds();
		OrderBy node = null;
		if(founds.isEmpty()){
			node = sqlRoot.orderBy();
		}else{
			node = (OrderBy)founds.get(0);
		}
		if(node.getChildren().isEmpty()){
			String alias = findFirstSqlTargetAlias();
			String identifier = alias + "." + ID_FIELD;
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
				.where()
					.andConds()
						.andSimpleCond("p.idNo LIKE :pIdNo", String.class, "P%").getRoot()
				.orderBy()
					.desc("p.idNo");
			List<Object> results = c.executeQueryPageable();
			results.forEach(o->{
				Member m = (Member)o;
				System.out.println(m.getName());
			});
		});
	}
	
	public static void main(String[]args){
		testExecuteQueryPageable();
	}
}
