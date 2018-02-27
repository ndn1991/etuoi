package com.xlot.admin;

import java.util.Arrays;
import java.util.UUID;

import com.nhb.common.utils.Converter;
import com.xlot.admin.bean.UserBean;
import com.xlot.admin.id.PasswordUtils;
import com.xlot.admin.id.UserStatus;
import com.xlot.admin.model.RbacModel;
import com.xlot.admin.model.UserModel;
import com.xlot.admin.statics.Permissions;
import com.xlot.admin.statics.Roles;

import scala.Tuple2;

public interface RbacInitializer {
	default void initRootUser(UserModel userModel, RbacModel rbacModel, String rootUser, String rootPassword) {
		Tuple2<String, String> tuple = PasswordUtils.getStoredPassword(rootPassword);
		long now = System.currentTimeMillis();
		UserBean user = new UserBean();
		user.setHashKey(tuple._1);
		user.setPassword(tuple._2);
		user.setSalt(tuple._1);
		user.setStatus(UserStatus.ACTIVE);
		user.setTimestamp(now);
		user.setUserId(Converter.uuidToBytes(UUID.randomUUID()));
		user.setUsername(rootUser);
		userModel.insertIgnore(user);
		
		byte[] roleId = Converter.uuidToBytes(UUID.randomUUID());
		rbacModel.insertRole(roleId, Roles.SUPER_USER, null, now);
		roleId = rbacModel.getRole(Roles.SUPER_USER).getId();
		
		byte[] permissionId = Converter.uuidToBytes(UUID.randomUUID());
		rbacModel.insertPermission(permissionId, Permissions.SUPER_USER, null);
		permissionId = rbacModel.getPermission(Permissions.SUPER_USER).getId();

		rbacModel.addRolePermissions(roleId, Arrays.asList(permissionId), now);
		
		user = userModel.loadByName(rootUser);
		rbacModel.grantRole(user.getUserId(), Arrays.asList(roleId), now);
	}
}
