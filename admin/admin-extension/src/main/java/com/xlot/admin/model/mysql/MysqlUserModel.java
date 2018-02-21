package com.xlot.admin.model.mysql;

import com.nhb.common.db.models.AbstractModel;
import com.xlot.admin.bean.UserBean;
import com.xlot.admin.dao.UserDAO;
import com.xlot.admin.model.UserModel;

public class MysqlUserModel extends AbstractModel implements UserModel {
	@Override
	protected void init() {
		super.init();
		if (getDbAdapter() == null) {
			throw new RuntimeException("db adapter can not empty");
		}
	}

	@Override
	public UserBean loadById(byte[] id) {
		try (UserDAO dao = getDbAdapter().openDAO(UserDAO.class)) {
			return dao.loadById(id);
		}
	}

	@Override
	public UserBean loadByName(String username) {
		try (UserDAO dao = getDbAdapter().openDAO(UserDAO.class)) {
			return dao.loadByName(username);
		}
	}

	@Override
	public int insertIgnore(UserBean bean) {
		try (UserDAO dao = getDbAdapter().openDAO(UserDAO.class)) {
			return dao.insertIgnore(bean);
		}
	}

	@Override
	public int updatePassword(byte[] id, String salt, String hashKey, String password, long timestamp) {
		try (UserDAO dao = getDbAdapter().openDAO(UserDAO.class)) {
			return dao.updatePassword(id, salt, hashKey, password, timestamp);
		}
	}

	@Override
	public int updateHashKey(byte[] id, String hashKey, long timestamp) {
		try (UserDAO dao = getDbAdapter().openDAO(UserDAO.class)) {
			return dao.updateHashKey(id, hashKey, timestamp);
		}
	}

}
