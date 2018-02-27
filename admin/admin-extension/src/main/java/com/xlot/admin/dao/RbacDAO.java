package com.xlot.admin.dao;

import java.util.Collection;
import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.Mapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;

import com.nhb.common.db.sql.daos.BaseMySqlDAO;
import com.xlot.admin.bean.PermissionBean;
import com.xlot.admin.bean.RoleBean;
import com.xlot.admin.dao.mapper.PermissionMapper;
import com.xlot.admin.dao.mapper.RoleMapper;

import ndn.SqlUtils;

@UseStringTemplate3StatementLocator
public abstract class RbacDAO extends BaseMySqlDAO {

	@SqlBatch("insert ignore into role_permission(role_id, permission_id, created_time) values(:roleId, :permissionId, :createdTime)")
	public abstract int[] addRolePermission(@Bind("roleId") List<byte[]> roleIds,
			@Bind("permissionId") Collection<byte[]> permissionIds, @Bind("createdTime") long createdTime);

	@SqlQuery("select role_id from grant_role where user_id=:userId")
	public abstract Collection<byte[]> getUserRoleIds(@Bind("userId") byte[] userId);

	@SqlUpdate("insert ignore into permission(id, name, description, created_time) values(:id, :name, :description, :createdTime)")
	public abstract int insertPermission(@Bind("id") byte[] id, @Bind("name") String name,
			@Bind("description") String description, @Bind("createdTime") long createdTime);

	@SqlQuery("select * from permission")
	@Mapper(PermissionMapper.class)
	public abstract List<PermissionBean> fetchPermissions();

	@SqlQuery("select p.* from permission p join role_permission rp on p.id=rp.permission_id where rp.role_id=:roleId")
	@Mapper(PermissionMapper.class)
	public abstract List<PermissionBean> fetchPermissions(@Bind("roleId") byte[] roleId);

	@SqlUpdate("insert ignore into role(id, name, description, created_time) values(:id, :name, :description, :createdTime)")
	public abstract int insertRole(@Bind("id") byte[] id, @Bind("name") String name,
			@Bind("description") String description, @Bind("createdTime") long createdTime);

	@SqlUpdate("delete from role_permission where role_id=:roleId and permission_id not in (<permissionIds>)")
	public abstract int removePermissionsExclude(@Bind("roleId") byte[] roleId,
			@BindIn("permissionIds") List<byte[]> permissionIds);

	@SqlQuery("select * from role")
	@Mapper(RoleMapper.class)
	public abstract List<RoleBean> fetchRoles();

	@SqlQuery("select r.* from role r join grant_role gr on gr.role_id=r.id where gr.user_id=:userId")
	@Mapper(RoleMapper.class)
	public abstract List<RoleBean> fetchRoles(@Bind("userId") byte[] accountId);

	@SqlBatch("INSERT IGNORE INTO grant_role(user_id, grant_by, role_id, granted_time)\n"
			+ "VALUES(:userId, :grantBy, :roleId, :grantedTime)")
	public abstract int[] grantRole(@Bind("grantBy") List<byte[]> adminIds, @Bind("userId") List<byte[]> userIds,
			@Bind("roleId") List<byte[]> roleIds, @Bind("grantedTime") long timestamp);

	@SqlUpdate("delete from grant_role where user_id=:userId and role_id not in (<roleIds>)")
	public abstract int removeRoleExclude(@Bind("userId") byte[] userId, @BindIn("roleIds") List<byte[]> roleIds);

	@SqlQuery("select * from role where name=:name")
	@Mapper(RoleMapper.class)
	public abstract RoleBean getRoleByName(@Bind("name") String roleName);

	public static void main(String[] args) {
		System.out.println(SqlUtils.insert("grant_role", "user_id", "grant_by", "role_id", "granted_time"));
	}

	@SqlBatch("INSERT IGNORE INTO grant_role(user_id, role_id, granted_time)\n"
			+ "VALUES(:userId, :roleId, :grantedTime)")
	public abstract int[] grantRole(@Bind("userId") List<byte[]> userIds, @Bind("roleId") List<byte[]> roleIds,
			@Bind("grantedTime") long timestamp);

	@SqlQuery("select * from permission where name=:name")
	@Mapper(PermissionMapper.class)
	public abstract PermissionBean getPermission(@Bind("name") String permissionName);

	@SqlQuery("select p.* from grant_role gr join role_permission rp on gr.role_id=rp.role_id join permission p on rp.permission_id=p.id where gr.user_id=:userId")
	@Mapper(PermissionMapper.class)
	public abstract List<PermissionBean> permissionOfUser(@Bind("userId") byte[] userId);

}
