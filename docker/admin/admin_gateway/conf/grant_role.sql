CREATE TABLE IF NOT EXISTS grant_role (
  user_id binary(16) NOT NULL,
  grant_by binary(16) DEFAULT NULL,
  role_id binary(16) NOT NULL,
  granted_time BIGINT(16) NOT NULL,
  PRIMARY KEY (user_id,role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
