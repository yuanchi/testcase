package com.jerrylin.erp.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

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
	private static void testListToStr(){
		List<String> ids = new ArrayList<>();
		ids.add("1");
		ids.add("2");
		System.out.println(ids.toString());
	}
	private static void testSaveBeforeDelete(){
		executeApplicationContext(acac->{
			SessionFactoryWrapper sfw = acac.getBean(SessionFactoryWrapper.class);
			SalesDetail d = sfw.executeTxReturnResults(s->{
				ScrollableResults results = s.createQuery("SELECT p FROM " + SalesDetail.class.getName() + " p WHERE p.id = :id").setString("id", "20160429-120527627-XDffi").scroll(ScrollMode.FORWARD_ONLY);
				List<SalesDetail> saved = new ArrayList<>();
				while(results.next()){
					Object target = results.get()[0];
					s.evict(target);
					saved.add((SalesDetail)target);
					s.delete(target);
				}
				s.flush();
				s.clear();
				return saved.get(0);
			});
			System.out.println(d.getId());
		});
	}
	public static void main(String[]args){
//		testSalesDetailToJson();
//		testSalesDetailManyToOne();
//		testListToStr();
		testSaveBeforeDelete();
	}
	
	
}
