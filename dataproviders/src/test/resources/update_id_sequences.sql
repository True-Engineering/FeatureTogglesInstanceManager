SELECT setval('acl_class_id_seq', (SELECT MAX(id) FROM acl_class)+1);
SELECT setval('acl_entry_id_seq', (SELECT MAX(id) FROM acl_entry)+1);
SELECT setval('acl_object_identity_id_seq', (SELECT MAX(id) FROM acl_object_identity)+1);
SELECT setval('acl_sid_id_seq', (SELECT MAX(id) FROM acl_sid)+1);

SELECT setval('env_instance_id_seq', (SELECT MAX(id) FROM env_instance)+1);
SELECT setval('environment_id_seq', (SELECT MAX(id) FROM environment)+1);
SELECT setval('feature_environment_state_id_seq', (SELECT MAX(id) FROM feature_environment_state)+1);
SELECT setval('features_id_seq', (SELECT MAX(id) FROM features)+1);
SELECT setval('organization_id_seq', (SELECT MAX(id) FROM organization)+1);
SELECT setval('project_id_seq', (SELECT MAX(id) FROM project)+1);
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users)+1);
