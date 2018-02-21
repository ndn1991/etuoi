package com.xlot.admin.model.mysql;

import java.util.List;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.IntegerColumnMapper;

import com.google.common.base.Strings;
import com.nhb.common.db.models.AbstractModel;
import com.xlot.admin.bean.UserWithRoleBean;
import com.xlot.admin.dao.mapper.UserWithRoleMapper;
import com.xlot.admin.model.UserRbacModel;
import com.xlot.admin.sql.AndClause;
import com.xlot.admin.sql.InClause;
import com.xlot.admin.sql.LikeBeforeClause;

public class MysqlUserRbacModel extends AbstractModel implements UserRbacModel {
	private static final String SQL_TEMPLATE = "select u.*, grr.roles \n" //
			+ "	from user u \n" //
			+ "    left join (select gr.user_id, group_concat(r.name) roles from grant_role gr join role r on gr.role_id=r.id group by gr.user_id) as grr\n"
			+ "		on u.user_id=grr.user_id\n" //
			+ "	where %s\n" //
			+ "    limit %d, %d;";
	private static final String SQL_COUNT_TEMPLATE = "select count(1) c from user where %s;";

	@Override
	public List<UserWithRoleBean> search(String username, int from, int size) {
		AndClause clause = new AndClause();
		if (!Strings.isNullOrEmpty(username)) {
			clause.clause(new LikeBeforeClause("username", username));
		}
		String sql = String.format(SQL_TEMPLATE, clause.sqlString(), from, size);
		try (Handle h = getDbAdapter().newHandle()) {
			return h.createQuery(sql).map(new UserWithRoleMapper()).list();
		}
	}

	@Override
	public int searchCount(String username) {
		AndClause clause = new AndClause();
		if (!Strings.isNullOrEmpty(username)) {
			clause.clause(new LikeBeforeClause("username", username));
		}
		String sql = String.format(SQL_COUNT_TEMPLATE, clause.sqlString());
		try (Handle h = getDbAdapter().newHandle()) {
			return h.createQuery(sql).map(IntegerColumnMapper.PRIMITIVE).first();
		}
	}

	@Override
	public List<UserWithRoleBean> searchOfProduct(List<byte[]> refIds, String username, int from, int size) {
		AndClause clause = new AndClause();
		if (!Strings.isNullOrEmpty(username)) {
			clause.clause(new LikeBeforeClause("username", username));
		}
		InClause inClause = new InClause().field("ref_id");
		for (byte[] refId : refIds) {
			inClause.value(refId);
		}
		clause.clause(inClause);

		String sql = String.format(SQL_TEMPLATE, clause.sqlString(), from, size);
		try (Handle h = getDbAdapter().newHandle()) {
			return h.createQuery(sql).map(new UserWithRoleMapper()).list();
		}
	}

	@Override
	public int searchOfProductCount(List<byte[]> refIds, String username) {
		AndClause clause = new AndClause();
		if (!Strings.isNullOrEmpty(username)) {
			clause.clause(new LikeBeforeClause("username", username));
		}
		InClause inClause = new InClause().field("ref_id");
		for (byte[] refId : refIds) {
			inClause.value(refId);
		}
		clause.clause(inClause);

		String sql = String.format(SQL_COUNT_TEMPLATE, clause.sqlString());
		try (Handle h = getDbAdapter().newHandle()) {
			return h.createQuery(sql).map(IntegerColumnMapper.PRIMITIVE).first();
		}
	}
}
