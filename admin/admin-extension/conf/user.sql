CREATE TABLE IF NOT EXISTS user(
	user_id BINARY(16) NOT NULL,
	username VARCHAR(100) NOT NULL,
	password VARCHAR(128) NOT NULL,
	salt VARCHAR(50) NOT NULL,
	hash_key VARCHAR(50) NOT NULL,
	status INT NOT NULL,
	timestamp BIGINT(16) NOT NULL,
	type INT DEFAULT 0,
	ref_id BINARY(16) DEFAULT NULL,
	
	PRIMARY KEY (user_id),
	UNIQUE INDEX username_unique USING BTREE (username ASC)
);