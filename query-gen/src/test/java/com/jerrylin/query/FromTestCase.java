package com.jerrylin.query;

import org.junit.Test;
import static org.junit.Assert.*;

public class FromTestCase {
	@Test
	public void pk(){
		SelectExpression root = 
			SelectExpression.init()
				.select()
					.t("p.code", "code")
					.t("p.price", "price")
					.t("p.memberPrice", "memberPrice").toRoot()
				.from()
					.t("product", "p")
					.pk("id")
					.leftOuterJoin()
					.t("sales", "s")
					.pk("salesId")
					.on("p.id = s.productId").toRoot()
					
		;
		String productPk = root.find(sn->{
				return (sn instanceof SimpleExpression) && ("p".equals(sn.attr(SimpleExpression.ALIAS)));
			})
			.getFound(SimpleExpression.class)
			.get(0)
			.attr(From.PK);
		String salesPk = root.find(sn->{
				return (sn instanceof SimpleExpression) && ("s".equals(sn.attr(SimpleExpression.ALIAS)));
			})
			.getFound(SimpleExpression.class)
			.get(0)
			.attr(From.PK);		
		assertEquals("product pk should be id", "id", productPk);
		assertEquals("sales pk should be salesId", "salesId", salesPk);
	}
}
