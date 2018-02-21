CREATE TABLE IF NOT EXISTS role (
  id binary(16) NOT NULL,
  name varchar(45) NOT NULL,
  description varchar(200) DEFAULT NULL,
  created_time BIGINT(16) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE INDEX name_unique USING BTREE (name ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
