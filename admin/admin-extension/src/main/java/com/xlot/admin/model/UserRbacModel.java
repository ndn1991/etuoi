package com.xlot.admin.model;

import java.util.List;

import com.xlot.admin.bean.UserWithRoleBean;

public interface UserRbacModel {
	List<UserWithRoleBean> search(String username, int from, int size);

	int searchCount(String username);

	List<UserWithRoleBean> searchOfProduct(List<byte[]> refIds, String username, int from, int size);

	int searchOfProductCount(List<byte[]> refIds, String username);
}
