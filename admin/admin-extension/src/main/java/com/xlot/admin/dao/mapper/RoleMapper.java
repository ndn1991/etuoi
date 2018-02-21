package com.xlot.admin.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.xlot.admin.bean.RoleBean;

import ndn.SqlUtils;

public class RoleMapper implements ResultSetMapper<RoleBean> {

	@Override
	public RoleBean map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		RoleBean bean = new RoleBean();
		bean.setId(r.getBytes("id"));
		bean.setDescription(r.getString("description"));
		bean.setCreatedTime(r.getLong("created_time"));
		bean.setName(r.getString("name"));
		return bean;
	}

	public static void main(String[] args) {
		System.out.println(SqlUtils.mapper(RoleBean.class));
	}
}
