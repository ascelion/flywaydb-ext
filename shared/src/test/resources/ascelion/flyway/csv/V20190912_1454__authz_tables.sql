CREATE TABLE authz_users
(
	id VARCHAR(36) NOT NULL DEFAULT random_uuid(),
	created_at TIMESTAMP NOT NULL DEFAULT current_timestamp(),
	updated_at TIMESTAMP NOT NULL DEFAULT current_timestamp(),

	username VARCHAR(50) NOT NULL UNIQUE,
	password VARCHAR(100) NOT NULL,
	disabled BOOLEAN NOT NULL DEFAULT false,

	PRIMARY KEY(id)
);

CREATE TABLE authz_roles
(
	id VARCHAR(36) NOT NULL DEFAULT random_uuid(),
	created_at TIMESTAMP NOT NULL DEFAULT current_timestamp(),
	updated_at TIMESTAMP NOT NULL DEFAULT current_timestamp(),

	rolename VARCHAR(50) NOT NULL,

	PRIMARY KEY(id)
);

CREATE TABLE authz_users_roles
(
	user_id VARCHAR(36) NOT NULL,
	role_id VARCHAR(36) NOT NULL,

	UNIQUE(user_id, role_id),

	FOREIGN KEY(user_id) REFERENCES authz_users(id),
	FOREIGN KEY(role_id) REFERENCES authz_roles(id),

	PRIMARY KEY(user_id, role_id)
);
