package com.jerrylin.erp.security;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.jerrylin.erp.security.extend.UserInfo;

@Entity
@Table(name = "shr_alluser")
public class User extends SecurityObject implements RoleBelong {
	private String userId;
	private String password;
	private Group defaultGroup;
	private Set<Group> groups = new HashSet<>();
	private Set<Role> roles = new HashSet<>();
	private boolean disabled = false;
	private UserInfo info;
	
	public User(){}
	public User(String userId, String password){
		this.userId = userId;
		this.password = password;
	}
	
	@Column(name = "userId", unique = true)
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	@Column(name = "password")
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "defaultGroup")
	public Group getDefaultGroup() {
		return defaultGroup;
	}
	public void setDefaultGroup(Group defaultGroup) {
		this.defaultGroup = defaultGroup;
	}
	@ManyToMany(targetEntity = Group.class, fetch = FetchType.LAZY)
	@JoinTable(
		name = "shr_group_user",
		joinColumns = @JoinColumn(name = "userId"),
		inverseJoinColumns = @JoinColumn(name = "groupId"))
	public Set<Group> getGroups() {
		return groups;
	}
	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}
	@ManyToMany(targetEntity = Role.class, fetch = FetchType.EAGER)
	@JoinTable(
		name = "shr_role_user",
		joinColumns = @JoinColumn(name = "userId"),
		inverseJoinColumns = @JoinColumn(name = "roleId")
	)
	public Set<Role> getRoles() {
		return roles;
	}
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	@Column(name = "disabled")
	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	@ManyToOne(targetEntity = UserInfo.class)
	@JoinColumn(name = "info")
	public UserInfo getInfo() {
		return info;
	}
	public void setInfo(UserInfo info) {
		this.info = info;
	}
	@Transient
	@Override
	public boolean isBelongToRole(String roleId){
		if("root".equals(getUserId())
		&& RoleConstants.ROOT.equals(roleId)){
			return true;
		}
		if(roles.contains(new Role(roleId))){
			return true;
		}
		boolean foundMatched = groups
			.stream()
			.anyMatch(g->{return isGroupBelongToRole(g, roleId);});
		if(foundMatched){
			return true;
		}
		return false;
	}
}
