package com.jerrylin.erp.security;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;

@Entity
@Table(name = "shr_allrole")
public class Role extends SecurityObject {
	private Set<Group> groups = new HashSet<>();
	private Set<User> users = new HashSet<>();
	public Role(){}
	public Role(String id){setId(id);}
	@ManyToMany(targetEntity = Group.class, fetch = FetchType.LAZY)
	@JoinTable(
		name = "shr_role_group",
		joinColumns = @JoinColumn(name = "roleId"),
		inverseJoinColumns = @JoinColumn(name = "groupId"))
	public Set<Group> getGroups() {
		return groups;
	}
	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}
	@ManyToMany(targetEntity = User.class, fetch = FetchType.LAZY)
	@JoinTable(
		name = "shr_role_user",
		joinColumns = @JoinColumn(name = "roleId"),
		inverseJoinColumns = @JoinColumn(name = "userId"))
	public Set<User> getUsers() {
		return users;
	}
	public void setUsers(Set<User> users) {
		this.users = users;
	}
	@Override
	public boolean equals(Object obj){
		if(obj==null || !(obj instanceof Role)){
			return false;
		}
		Role role = (Role)obj;
		return new EqualsBuilder().append(role.getId(), getId()).isEquals();
	}
}
