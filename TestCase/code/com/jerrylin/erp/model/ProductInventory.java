package com.jerrylin.erp.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="product_inventory")
public class ProductInventory {
	private String id;
	private InventoryOption invOption;
	private String productId;
	private int stockQty;
	@Id
	@Column(name="id")
	@GenericGenerator(name="jerrylin_product_inventory_id", strategy="com.jerrylin.erp.ds.TimeUID")
	@GeneratedValue(generator="jerrylin_product_inventory_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="invOptionId")
	public InventoryOption getInvOption() {
		return invOption;
	}
	public void setInvOption(InventoryOption invOption) {
		this.invOption = invOption;
	}
	@JoinColumn(name="productId")
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	@Column(name="stockQty")
	public int getStockQty() {
		return stockQty;
	}
	public void setStockQty(int stockQty) {
		this.stockQty = stockQty;
	}
}
