package com.xlot.admin.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import com.nhb.common.db.sql.daos.BaseMySqlDAO;
import com.xlot.admin.bean.UserBean;
import com.xlot.admin.dao.mapper.UserMapper;

import ndn.SqlUtils;

@RegisterMapper(UserMapper.class)
public abstract class UserDAO extends BaseMySqlDAO {
	private static final String INSERT_IGNORE = "INSERT IGNORE INTO user(\n" + 
			"	user_id,\n" + 
			"	username,\n" + 
			"	password,\n" + 
			"	salt,\n" + 
			"	hash_key,\n" + 
			"	status,\n" + 
			"	timestamp,\n" +
			"	type,\n" +
			"	ref_id)\n" + 
			"VALUES(\n" + 
			"	:userId,\n" + 
			"	:username,\n" + 
			"	:password,\n" + 
			"	:salt,\n" + 
			"	:hashKey,\n" + 
			"	:status,\n" + 
			"	:timestamp,\n" +
			"	:type,\n" +
			"	:refId)";

	@SqlUpdate(INSERT_IGNORE)
	public abstract int insertIgnore(@BindBean UserBean user);

	@SqlQuery("select * from user where user_id=:id")
	public abstract UserBean loadById(@Bind("id") byte[] id);

	@SqlQuery("select * from user where username=:username")
	public abstract UserBean loadByName(@Bind("username") String username);

	@SqlUpdate("update user set salt=:salt, hash_key=:hashKey, password=:password, timestamp=:timestamp where user_id=:id")
	public abstract int updatePassword(@Bind("id") byte[] id, @Bind("salt") String salt,
			@Bind("hashKey") String hashKey, @Bind("password") String password, @Bind("timestamp") long timestamp);
	
	@SqlUpdate("update user set hash_key=:hashKey, timestamp=:timestamp where user_id=:id")
	public abstract int updateHashKey(@Bind("id") byte[] id, @Bind("hashKey") String hashKey, @Bind("timestamp") long timestamp);
	
	public static void main(String[] args) {
		System.out.println(SqlUtils.insert("user", "user_id", "username", "password", "salt", "hash_key", "status", "timestamp"));
	}
}
