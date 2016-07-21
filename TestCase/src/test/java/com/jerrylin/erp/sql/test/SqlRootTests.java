package com.jerrylin.erp.sql.test;

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.jerrylin.erp.security.User;
import com.jerrylin.erp.sql.ISqlRoot;
import com.jerrylin.erp.sql.Join;
import com.jerrylin.erp.sql.SqlRoot;

public class SqlRootTests {
	@Test
	public void findMultiple(){
		ISqlRoot root = SqlRoot.getInstance()
			.select()
				.target("p.id", "pId").getRoot()
			.from()
				.target(User.class, "p").getRoot()
			.joinAlias("LEFT JOIN p.users", "users")
			.joinAlias("LEFT JOIN p.groups", "groups");
		List<Join> joins = root.findMultiple(Join.class);
		System.out.println(joins.size());
		assertTrue(!joins.isEmpty());
	}
	@Test
	public void isInstance(){
		Join join = new Join();
		System.out.println(join.getClass().isInstance(Join.class));
	}
}
