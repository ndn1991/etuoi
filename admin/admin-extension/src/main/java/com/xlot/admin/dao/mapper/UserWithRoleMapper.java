package com.xlot.admin.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.google.common.base.Strings;
import com.xlot.admin.bean.UserWithRoleBean;

public class UserWithRoleMapper implements ResultSetMapper<UserWithRoleBean> {
	private static final String REF_ID = "ref_id";
	private static final String TYPE = "type";
	private static final String HASH_KEY = "hash_key";
	private static final String TIMESTAMP = "timestamp";
	private static final String STATUS = "status";
	private static final String SALT = "salt";
	private static final String PASSWORD = "password";
	private static final String USERNAME = "username";
	private static final String USER_ID = "user_id";

	@Override
	public UserWithRoleBean map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		UserWithRoleBean rs = new UserWithRoleBean();
		rs.setUserId(r.getBytes(USER_ID));
		rs.setUsername(r.getString(USERNAME));
		rs.setPassword(r.getString(PASSWORD));
		rs.setSalt(r.getString(SALT));
		rs.setStatus(r.getInt(STATUS));
		rs.setTimestamp(r.getLong(TIMESTAMP));
		rs.setHashKey(r.getString(HASH_KEY));
		rs.setType(r.getInt(TYPE));
		rs.setRefId(r.getBytes(REF_ID));
		String roleNames = r.getString("roles");
		if (!Strings.isNullOrEmpty(roleNames)) {
			rs.setRoleNames(Arrays.asList(roleNames.split(",")));
		}
		return rs;
	}

}
