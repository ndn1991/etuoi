CREATE TABLE IF NOT EXISTS role_permission (
  role_id binary(16) NOT NULL,
  permission_id binary(16) NOT NULL,
  created_time BIGINT(16) NOT NULL,
  PRIMARY KEY (role_id,permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
