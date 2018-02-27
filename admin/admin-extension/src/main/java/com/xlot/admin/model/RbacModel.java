package com.xlot.admin.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.xlot.admin.bean.PermissionBean;
import com.xlot.admin.bean.RoleBean;

public interface RbacModel {

	Collection<byte[]> getRoleIds(byte[] userId);

	int insertPermission(byte[] id, String name, String description);

	List<PermissionBean> getPermissions();

	List<PermissionBean> getPermission(byte[] roleId);

	int insertRole(byte[] id, String name, String description, long timestamp);

	void addRolePermissions(byte[] id, List<byte[]> permissionIds, long timestamp);

	void removePermissionsExclude(byte[] roleId, List<byte[]> permissionIds);

	List<RoleBean> getRoles();
	
	RoleBean getRole(String roleName);

	List<RoleBean> getRoles(byte[] accountId);

	void grantRole(byte[] adminId, byte[] userId, List<byte[]> roleIds, long timestamp);
	
	void grantRole(byte[] userId, List<byte[]> roleIds, long timestamp);

	int removeRoleExclude(byte[] adminId, byte[] userId, List<byte[]> roleIds);

	default <T> List<T> createAndFill(T obj, int n) {
		List<T> list = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			list.add(obj);
		}
		return list;
	}

	PermissionBean getPermission(String permissionName);
	
	List<PermissionBean> permissionsOfUser(byte[] userId);
}
