package com.xlot.admin.model.mysql;

import java.util.Collection;
import java.util.List;

import com.nhb.common.db.models.AbstractModel;
import com.xlot.admin.bean.PermissionBean;
import com.xlot.admin.bean.RoleBean;
import com.xlot.admin.dao.RbacDAO;
import com.xlot.admin.model.RbacModel;

public class MysqlRbacModel extends AbstractModel implements RbacModel {

	@Override
	public Collection<byte[]> getRoleIds(byte[] userId) {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			return dao.getUserRoleIds(userId);
		}
	}

	@Override
	public int insertPermission(byte[] id, String name, String description) {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			return dao.insertPermission(id, name, description, System.currentTimeMillis());
		}
	}

	@Override
	public List<PermissionBean> getPermissions() {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			return dao.fetchPermissions();
		}
	}

	@Override
	public List<PermissionBean> getPermission(byte[] roleId) {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			return dao.fetchPermissions(roleId);
		}
	}

	@Override
	public int insertRole(byte[] id, String name, String description, long timestamp) {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			return dao.insertRole(id, name, description, timestamp);
		}
	}

	@Override
	public void addRolePermissions(byte[] id, List<byte[]> permissionIds, long timestamp) {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			dao.addRolePermission(createAndFill(id, permissionIds.size()), permissionIds, timestamp);
		}
	}

	@Override
	public void removePermissionsExclude(byte[] roleId, List<byte[]> permissionIds) {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			dao.removePermissionsExclude(roleId, permissionIds);
		}
	}

	@Override
	public List<RoleBean> getRoles() {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			return dao.fetchRoles();
		}
	}

	@Override
	public List<RoleBean> getRoles(byte[] accountId) {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			return dao.fetchRoles(accountId);
		}
	}

	@Override
	public void grantRole(byte[] adminId, byte[] userId, List<byte[]> roleIds, long timestamp) {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			int n = roleIds.size();
			dao.grantRole(createAndFill(adminId, n), createAndFill(userId, n), roleIds, timestamp);
		}
	}
	
	@Override
	public void grantRole(byte[] userId, List<byte[]> roleIds, long timestamp) {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			dao.grantRole(createAndFill(userId, roleIds.size()), roleIds, timestamp);
		}
	}

	@Override
	public int removeRoleExclude(byte[] adminId, byte[] userId, List<byte[]> roleIds) {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			return dao.removeRoleExclude(userId, roleIds);
		}
	}

	@Override
	public RoleBean getRole(String roleName) {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			return dao.getRoleByName(roleName);
		}
	}

	@Override
	public PermissionBean getPermission(String permissionName) {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			return dao.getPermission(permissionName);
		}
	}

	@Override
	public List<PermissionBean> permissionsOfUser(byte[] userId) {
		try (RbacDAO dao = getDbAdapter().openDAO(RbacDAO.class)) {
			return dao.permissionOfUser(userId);
		}
	}

}
