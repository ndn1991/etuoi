package com.xlot.admin.model;

import com.xlot.admin.bean.UserBean;

public interface UserModel {

	UserBean loadById(byte[] id);

	UserBean loadByName(String username);

	int insertIgnore(UserBean bean);

	int updatePassword(byte[] id, String salt, String hashKey, String password, long timestamp);

	int updateHashKey(byte[] id, String hashKey, long timestamp);
}