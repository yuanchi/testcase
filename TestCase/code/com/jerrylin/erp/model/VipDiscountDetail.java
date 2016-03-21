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
@Table(name="vipdiscountdetail")
public class VipDiscountDetail {
	private String id;
	private String memberId;
	private String memberIdNo;
	private Date effectiveStart;
	private Date effectiveEnd;
	private Date discountUseDate;
	private Date toVipDate;
	private boolean available;
	private String useStatus;
	
	@Id
	@Column(name="id")
	@GenericGenerator(name = "vip_discount_detail_id", strategy = "com.jerrylin.erp.ds.TimeUID")
	@GeneratedValue(generator = "vip_discount_detail_id")
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
	@Column(name="memberIdNo")
	public String getMemberIdNo() {
		return memberIdNo;
	}
	public void setMemberIdNo(String memberIdNo) {
		this.memberIdNo = memberIdNo;
	}
	@Column(name="effectiveStart")
	public Date getEffectiveStart() {
		return effectiveStart;
	}
	public void setEffectiveStart(Date effectiveStart) {
		this.effectiveStart = effectiveStart;
	}
	@Column(name="effectiveEnd")
	public Date getEffectiveEnd() {
		return effectiveEnd;
	}
	public void setEffectiveEnd(Date effectiveEnd) {
		this.effectiveEnd = effectiveEnd;
	}
	@Column(name="discountUseDate")
	public Date getDiscountUseDate() {
		return discountUseDate;
	}
	public void setDiscountUseDate(Date discountUseDate) {
		this.discountUseDate = discountUseDate;
	}
	public Date getToVipDate() {
		return toVipDate;
	}
	@Column(name="toVipDate")
	public void setToVipDate(Date toVipDate) {
		this.toVipDate = toVipDate;
	}
	@Transient
	public boolean isAvailable(){
		return this.available;
	}
	public void setAvailable(boolean available){
		this.available = available;
	}
	@Transient
	public String getUseStatus() {
		return useStatus;
	}
	public void setUseStatus(String useStatus) {
		this.useStatus = useStatus;
	}
}
