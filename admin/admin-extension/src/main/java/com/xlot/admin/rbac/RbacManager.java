package com.xlot.admin.rbac;

import java.util.Collection;

public interface RbacManager {
	Collection<String> getPermissionsByRole(String role);

	boolean checkPermission(String role, String permission);

	void addRole(String role);

	boolean roleExisted(String role);

	Collection<String> getAllRoles();

	int addPermission(String role, String... permissions);

	int addUserRole(byte[] userId, String... roles);

	Collection<String> getUserRoles(byte[] userId);

	int removeUserRole(byte[] userId, String role);
}
