package com.jerrylin.erp.security;

import java.util.Set;


public interface RoleBelong {
	public boolean isBelongToRole(String roleId);
	public Set<Role> getRoles();
	public void setRoles(Set<Role> roles);
	default boolean isGroupBelongToRole(Group group, String roleId){
		if(group.isBelongToRole(roleId)){
			return true;
		}
		if(group.getParent() == null){
			return false;
		}
		return isGroupBelongToRole(group.getParent(), roleId);
	}
}
