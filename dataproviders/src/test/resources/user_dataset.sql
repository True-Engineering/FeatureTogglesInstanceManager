TRUNCATE users CASCADE;
INSERT INTO users (id, email, user_name, user_status, last_login, created, updated)
VALUES (1, 'email', 'name', 'ACTIVE', now(), now(), now());
INSERT INTO users (id, email, user_name, user_status, last_login, created, updated, removed)
VALUES (2, 'email2', 'name2', 'ACTIVE', now(), now(), now(), true)