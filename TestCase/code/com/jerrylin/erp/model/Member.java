package com.jerrylin.erp.model;

import java.sql.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="member")
public class Member {
	@Transient
	public static final int GENDER_MALE = 0;
	@Transient
	public static final int GENDER_FEMALE = 1;
	
	private String id;
	private boolean important;
	private String name;
	private String nameEng;
	private String fbNickname;
	private int gender;
	private String idNo;
	private Date birthday; 
	private String email;
	private String mobile;
	private String tel;
	private String postalCode;
	private String address;
	private Date toVipDate; // 轉VIP起始日
	private Date toVipEndDate; // VIP到期日
	private String note;
	private String clientId;
	private List<VipDiscountDetail> vipDiscountDetails = new LinkedList<>();
	
	@Id
	@Column(name="id")
	@GenericGenerator(name = "angrycat_member_id", strategy = "com.jerrylin.erp.ds.TimeUID")
	@GeneratedValue(generator = "angrycat_member_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="important")
	public boolean isImportant() {
		return important;
	}
	public void setImportant(boolean important) {
		this.important = important;
	}
	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Column(name="nameEng")
	public String getNameEng() {
		return nameEng;
	}
	public void setNameEng(String nameEng) {
		this.nameEng = nameEng;
	}
	@Column(name="gender")
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	@Column(name="idNo")
	public String getIdNo() {
		return idNo;
	}
	public void setIdNo(String idNo) {
		this.idNo = idNo;
	}
	@Column(name="birthday")
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	@Column(name="email")
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	@Column(name="mobile")
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	@Column(name="postalCode")
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	@Column(name="address")
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	@Column(name="toVipDate")
	public Date getToVipDate() {
		return toVipDate;
	}
	public void setToVipDate(Date toVipDate) {
		this.toVipDate = toVipDate;
	}
	@Column(name="note")
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	@Column(name="fbNickname")
	public String getFbNickname() {
		return fbNickname;
	}
	public void setFbNickname(String fbNickname) {
		this.fbNickname = fbNickname;
	}
	@Column(name="toVipEndDate")
	public Date getToVipEndDate() {
		return toVipEndDate;
	}
	public void setToVipEndDate(Date toVipEndDate) {
		this.toVipEndDate = toVipEndDate;
	}
	@OneToMany(fetch=FetchType.LAZY, targetEntity=VipDiscountDetail.class, cascade=CascadeType.ALL, mappedBy="memberId", orphanRemoval=true)
	@OrderBy("effectiveStart DESC")
	public List<VipDiscountDetail> getVipDiscountDetails() {
		return vipDiscountDetails;
	}
	public void setVipDiscountDetails(List<VipDiscountDetail> vipDiscountDetails) {
		this.vipDiscountDetails = vipDiscountDetails;
	}
	@Column(name="tel")
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	@Column(name="clientId")
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
}
