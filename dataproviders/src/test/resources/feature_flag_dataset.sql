TRUNCATE ORGANIZATION CASCADE;
TRUNCATE project CASCADE;
TRUNCATE environment CASCADE;

INSERT INTO ORGANIZATION (ID, ORGANIZATION_NAME)
values (1, 'Org');
insert into project (id, project_name, organization_id, removed)
values (1, 'Super test project 1', 1, false);
insert into project (id, project_name, organization_id, removed)
values (2, 'Super test project 2', 1, false);
insert into environment (id, environment_name, project_id, auth_key_hash)
values (1, 'TEST', 1, 'AUTH_KEY_HASH');
insert into environment (id, environment_name, project_id, auth_key_hash)
values (2, 'TEST2', 1, 'AUTH_KEY_HASH2');

insert into features (id, feat_uid, description, feature_group, feature_type, sprint, project_id, updated, feature_tag)
values (1, 'cool.feature.enabled', 'Cool feature flag', 'group', 'RELEASE', 'sprint 1', 1, NOW() - INTERVAL '1 days', 'WEB');
insert into feature_environment_state (feature_id, environment_id, strategy, strategy_params, enable)
VALUES (1, 1, 'CoolStrategy', '{"param1" : "value1"}', true);

insert into features (id, feat_uid, description, feature_group, feature_type, sprint, project_id, updated, feature_tag)
values (2, 'second.cool.feature.enabled', 'Cool feature flag', 'group', 'RELEASE', 'sprint 1', 1, NOW() - INTERVAL '2 days', 'TEST');
insert into feature_environment_state (feature_id, environment_id, strategy, strategy_params, enable)
VALUES (2, 1, 'CoolStrategy', '{"param1" : "value1"}', false);

insert into features (id, feat_uid, description, feature_group, feature_type, sprint, project_id, updated)
values (3, 'third.cool.feature.enabled', 'Cool feature flag', 'group', 'RELEASE', 'sprint 1', 2, NOW() - INTERVAL '3 days');
insert into feature_environment_state (feature_id, environment_id, strategy, strategy_params, enable)
VALUES (3, 2, 'CoolStrategy', '{"param1" : "value1"}', false);