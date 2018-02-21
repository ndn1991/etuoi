package com.xlot.admin.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import com.xlot.admin.bean.PermissionBean;

import ndn.SqlUtils;

public class PermissionMapper implements ResultSetMapper<PermissionBean> {

	public static void main(String[] args) {
		System.out.println(SqlUtils.mapper(PermissionBean.class));
	}

	@Override
	public PermissionBean map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		PermissionBean bean = new PermissionBean();
		bean.setId(r.getBytes("id"));
		bean.setDescription(r.getString("description"));
		bean.setCreatedTime(r.getLong("created_time"));
		bean.setName(r.getString("name"));
		return bean;
	}
}
