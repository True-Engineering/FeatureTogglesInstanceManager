TRUNCATE users CASCADE;
TRUNCATE ff_changes_history CASCADE;
TRUNCATE features CASCADE;
TRUNCATE environment CASCADE;
TRUNCATE project CASCADE;
TRUNCATE organization CASCADE;
TRUNCATE ff_changes_history CASCADE;

INSERT INTO organization(id, organization_name) VALUES (1, 'org');
INSERT INTO users(id, user_name, email) VALUES (1, 'name', 'email');
INSERT INTO project(id, project_name, organization_id) VALUES (1, 'project', 1);
INSERT INTO project(id, project_name, organization_id) VALUES (2, 'another project', 1);
INSERT INTO features(id, feat_uid, project_id) VALUES (1, 'one', 1);
INSERT INTO features(id, feat_uid, project_id) VALUES (2, 'two', 2);
INSERT INTO environment(id, environment_name, project_id) VALUES (1, 'env', 1);
INSERT INTO ff_changes_history(id, project_id, feature_id, user_id, environment_id, action) VALUES (10, 1, 1, 1, null, 'CREATE');