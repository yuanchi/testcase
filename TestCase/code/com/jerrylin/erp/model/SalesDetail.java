package com.jerrylin.erp.model;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;
@Entity
@Table(name="salesdetail")
public class SalesDetail {
	@Transient
	public static final String SALE_POINT_FB = "FB社團";
	@Transient
	public static final String SALE_POINT_ESLITE_DUNNAN = "敦南誠品";
	
	private String id;
	private String memberId;
	
	private String salePoint;
	private String saleStatus;
	private String fbName;
	private String activity;
	private String modelId;
	private String productName;
	private double price;
	private double memberPrice;
	private String priority;
	private Date orderDate;
	private String otherNote;
	private String checkBillStatus;
	private String idNo;
	private String discountType;
	private String arrivalStatus;
	private Date shippingDate;
	private String sendMethod;
	private String note;
	
	private Date payDate;
	private String contactInfo;
	private String registrant;
	
	private String rowId;
	
	@Id
	@Column(name="id")
	@GenericGenerator(name="angrycat_salesdetail_id", strategy="com.jerrylin.erp.ds.TimeUID")
	@GeneratedValue(generator = "angrycat_salesdetail_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="memberId")
	public String getMemberId() {
		return memberId;
	}
	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}
	@Column(name="salePoint")
	public String getSalePoint() {
		return salePoint;
	}
	public void setSalePoint(String salePoint) {
		this.salePoint = salePoint;
	}
	@Column(name="saleStatus")
	public String getSaleStatus() {
		return saleStatus;
	}
	public void setSaleStatus(String saleStatus) {
		this.saleStatus = saleStatus;
	}
	@Column(name="fbName")
	public String getFbName() {
		return fbName;
	}
	public void setFbName(String fbName) {
		this.fbName = fbName;
	}
	@Column(name="activity")
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	@Column(name="modelId")
	public String getModelId() {
		return modelId;
	}
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}
	@Column(name="productName")
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	@Column(name="price")
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	@Column(name="memberPrice")
	public double getMemberPrice() {
		return memberPrice;
	}
	public void setMemberPrice(double memberPrice) {
		this.memberPrice = memberPrice;
	}
	@Column(name="priority")
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	@Column(name="orderDate")
	public Date getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}
	@Column(name="otherNote")
	public String getOtherNote() {
		return otherNote;
	}
	public void setOtherNote(String otherNote) {
		this.otherNote = otherNote;
	}
	@Column(name="checkBillStatus")
	public String getCheckBillStatus() {
		return checkBillStatus;
	}
	public void setCheckBillStatus(String checkBillStatus) {
		this.checkBillStatus = checkBillStatus;
	}
	@Column(name="idNo")
	public String getIdNo() {
		return idNo;
	}
	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}
	@Column(name="discountType")
	public String getDiscountType() {
		return discountType;
	}
	public void setDiscountType(String discountType) {
		this.discountType = discountType;
	}
	@Column(name="arrivalStatus")
	public String getArrivalStatus() {
		return arrivalStatus;
	}
	public void setArrivalStatus(String arrivalStatus) {
		this.arrivalStatus = arrivalStatus;
	}
	@Column(name="shippingDate")
	public Date getShippingDate() {
		return shippingDate;
	}
	public void setShippingDate(Date shippingDate) {
		this.shippingDate = shippingDate;
	}
	@Column(name="sendMethod")
	public String getSendMethod() {
		return sendMethod;
	}
	public void setSendMethod(String sendMethod) {
		this.sendMethod = sendMethod;
	}
	@Column(name="note")
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	@Column(name="payDate")
	public Date getPayDate() {
		return payDate;
	}
	public void setPayDate(Date payDate) {
		this.payDate = payDate;
	}
	@Column(name="contactInfo")
	public String getContactInfo() {
		return contactInfo;
	}
	public void setContactInfo(String contactInfo) {
		this.contactInfo = contactInfo;
	}
	@Column(name="registrant")
	public String getRegistrant() {
		return registrant;
	}
	public void setRegistrant(String registrant) {
		this.registrant = registrant;
	}
	@Column(name="rowId")
	public String getRowId() {
		return rowId;
	}
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}
}
