package com.jerrylin.erp.hql.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.CacheMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jerrylin.erp.component.SessionFactoryWrapper;
import com.jerrylin.erp.initialize.config.RootConfig;
import com.jerrylin.erp.model.InventoryOption;
import com.jerrylin.erp.model.Product;
import com.jerrylin.erp.model.ProductInventory;



@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class GeneralQueryTests {
	@Autowired
	private SessionFactoryWrapper sfw;
	private InventoryOption findOrNewByName(Session s, String name, int plusOrMinus){
		List<InventoryOption> opts = s.createQuery("FROM " + InventoryOption.class.getName() + " p WHERE p.name = :name").setString("name", name).list();
		InventoryOption opt = null;
		if(opts.isEmpty()){
			opt = new InventoryOption();
			opt.setName(name);
			opt.setPlusOrMinus(plusOrMinus);
			s.save(opt);
			s.flush();
		}else{
			opt = opts.get(0);
		}
		return opt;
	}
	@Test
	public void insertInventoryOptions(){
		List<String> modelIds = Arrays.asList("AAA001", "AAA002", "AAA003");
		Map<String, Integer> invs = new LinkedHashMap<>();
		invs.put("總庫存", 0);
		invs.put("辦公室庫存", 1);
		invs.put("待出貨", -1);
		
		sfw.executeTransaction(s->{
			List<InventoryOption> ios = invs.entrySet().stream().map(e->findOrNewByName(s, e.getKey(), e.getValue())).collect(Collectors.toList());
//			ScrollableResults products = s.createQuery("SELECT DISTINCT p FROM " + Product.class.getName() + " p WHERE p.modelId IN (:modelIds)").setParameterList("modelIds", modelIds)
//				.setCacheMode(CacheMode.IGNORE)
//				.scroll(ScrollMode.FORWARD_ONLY);
			int batchSize = sfw.getBatchSize();
			ScrollableResults products = s.createQuery("SELECT DISTINCT p FROM " + Product.class.getName() + " p")
				.setCacheMode(CacheMode.IGNORE)
				.scroll(ScrollMode.FORWARD_ONLY);
			int count=0;
			while(products.next()){
				Product p = (Product)products.get()[0];
				List<ProductInventory> pis = new ArrayList<>();
				for(InventoryOption io : ios){
					ProductInventory pi = new ProductInventory();
					pi.setInvOption(io);
					pi.setProductId(p.getId());
					s.save(pi);
					s.flush();
					pis.add(pi);
				}
				p.getProductInventories().addAll(pis);
				s.saveOrUpdate(p);
				if(++count % batchSize == 0){
					s.flush();
					s.clear();
				}
			}
		});
	}
	@Test
	public void queryByIdx(){
		sfw.executeSession(s->{
			String q = "FROM " + Product.class.getName() + " p WHERE p.productInventories[0].stockQty = :stockQty";
			List<Product> p = s.createQuery(q).setInteger("stockQty", 0).list();
			System.out.println(p.size());
		});
	}
	@Test
	public void namedParam(){
		String q = 
		"SELECT DISTINCT p.id "+
		"FROM com.jerrylin.erp.model.Product p "+
		"WHERE p.productInventories[0].stockQty = :pProductInventories_0_StockQty_FILTER_1";
		sfw.executeSession(s->{
			List<Product> p = s.createQuery(q).setInteger("pProductInventories_0_StockQty_FILTER_1", 2).list();
			System.out.println(p.size());
		});
	}
}
