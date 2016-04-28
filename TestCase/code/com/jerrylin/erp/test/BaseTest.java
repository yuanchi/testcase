package com.jerrylin.erp.test;

import java.lang.reflect.Field;
import java.util.function.Consumer;


import org.springframework.context.annotation.AnnotationConfigApplicationContext;


import com.jerrylin.erp.component.SessionFactoryWrapper;
import com.jerrylin.erp.initialize.config.RootConfig;
import com.jerrylin.erp.jackson.mixin.MemberIgnoreDetail;
import com.jerrylin.erp.model.Member;
import com.jerrylin.erp.model.SalesDetail;
import com.jerrylin.erp.service.ModelPropertyService;
import com.jerrylin.erp.sql.SqlRoot;
import com.jerrylin.erp.sql.condition.StrCondition.MatchMode;
import com.jerrylin.erp.util.JsonParseUtil;


public class BaseTest {
	public static void executeApplicationContext(Consumer<AnnotationConfigApplicationContext> consumer){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		consumer.accept(acac);
		acac.close();
	}
	private static void testSalesDetailToJson(){
		executeApplicationContext(acac->{
			SessionFactoryWrapper sfw = acac.getBean(SessionFactoryWrapper.class);
			SalesDetail s = sfw.executeTxReturnResults(session->{
				String q = "SELECT p FROM " + SalesDetail.class.getName() + " p WHERE p.id = :id";
				SalesDetail d = (SalesDetail)session.createQuery(q).setString("id", "20160311-114302916-zkaJF").uniqueResult();
				return d;
			});
			
			
			String str = JsonParseUtil.parseToJson(s, Member.class, MemberIgnoreDetail.class);
			System.out.println(str);
		});
	}
	private static void testGetClassFieldType(){
		try {
			Field[] fields = Member.class.getDeclaredFields();
			for(Field f : fields){
				System.out.println(f.getName() + ":" + f.getType());
			}
			
			
		}  catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void testGetPropertyTypeMapping(){
		executeApplicationContext(acac->{
			ModelPropertyService m = acac.getBean(ModelPropertyService.class);
			
		});
	}
	
	private static void testSalesDetailManyToOne(){
		executeApplicationContext(acac->{
			SqlRoot s = acac.getBean(SqlRoot.class);
			s.select()
				.target("p").getRoot()
			.from()
				.target(SalesDetail.class, "p").getRoot()
			.where()
				.andConds()
					.andStrCondition("p.member.name = :pMemberName", MatchMode.ANYWHERE);
			System.out.println(s.genSql());
				
		});
	}	
	public static void main(String[]args){
//		testSalesDetailToJson();
		testSalesDetailManyToOne();
	}
	
	
}
