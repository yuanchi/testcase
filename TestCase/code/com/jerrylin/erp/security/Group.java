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
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.jerrylin.erp.security.extend.GroupInfo;

@Entity
@Table(name = "shr_allgroup")
public class Group extends SecurityObject implements RoleBelong {
	public static final int TYPE_GROUP = 0;	// 群組
	public static final int TYPE_ORG = 1;	// 部門
	private int type = TYPE_GROUP;
	private Group parent;
	private Set<Group> children = new HashSet<>();
	private Set<User> users = new HashSet<>();
	private Set<Role> roles = new HashSet<>();
	private GroupInfo info;
	
	@Column(name = "type")
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	@ManyToOne(targetEntity = Group.class)
	@JoinColumn(name = "parentId")
	public Group getParent() {
		return parent;
	}
	public void setParent(Group parent) {
		this.parent = parent;
	}
	@OneToMany(targetEntity = Group.class, fetch = FetchType.LAZY)
	public Set<Group> getChildren() {
		return children;
	}
	public void setChildren(Set<Group> children) {
		this.children = children;
	}
	@ManyToMany(targetEntity = User.class, fetch = FetchType.LAZY)
	@JoinTable(
		name = "shr_group_user",
		joinColumns = @JoinColumn(name = "groupId"),
		inverseJoinColumns = @JoinColumn(name = "userId"))
	public Set<User> getUsers() {
		return users;
	}
	public void setUsers(Set<User> users) {
		this.users = users;
	}
	@ManyToMany(targetEntity = Role.class, fetch = FetchType.LAZY)
	@JoinTable(
		name = "shr_role_group",
		joinColumns = @JoinColumn(name = "groupId"),
		inverseJoinColumns = @JoinColumn(name = "roleId"))
	public Set<Role> getRoles() {
		return roles;
	}
	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	@ManyToOne(targetEntity = GroupInfo.class)
	@JoinColumn(name = "info")
	public GroupInfo getInfo() {
		return info;
	}
	public void setInfo(GroupInfo info) {
		this.info = info;
	}
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Group)){
			return false;
		}
		Group group = (Group)obj;
		return new EqualsBuilder().append(group.getId(), getId()).isEquals();
	}
	@Override
	public boolean isBelongToRole(String roleId){
		if(roles.contains(new Role(roleId))){
			return true;
		}
		if(getParent() == null){
			return false;
		}
		if(isGroupBelongToRole(getParent(), roleId)){
			return true;
		}
		return false;
	}

}
