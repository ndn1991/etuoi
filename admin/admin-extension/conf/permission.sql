CREATE TABLE IF NOT EXISTS permission (
  id binary(16) NOT NULL,
  name varchar(45) DEFAULT NULL,
  description varchar(200) DEFAULT NULL,
  created_time BIGINT(16) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY name_unique (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
