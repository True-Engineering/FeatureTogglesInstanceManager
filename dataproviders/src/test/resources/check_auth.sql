TRUNCATE acl_sid CASCADE;
TRUNCATE acl_class CASCADE;
TRUNCATE acl_object_identity CASCADE;
TRUNCATE acl_entry CASCADE;

INSERT INTO acl_sid (id, principal, sid)
VALUES (1, true, 'user_1'),
       (2, true, 'user_2'),
       (3, false, 'FEATURE_FLAGS_ADMIN');

INSERT INTO acl_class (id, class)
VALUES
    (1, 'ru.trueengineering.featureflag.manager.core.domen.organization.Organization'),
    (2, 'ru.trueengineering.featureflag.manager.core.domen.project.Project'),
    (3, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment')
;

INSERT INTO acl_object_identity
(id, object_id_class, object_id_identity,
 parent_object, owner_sid, entries_inheriting)
VALUES (1, 1, '1', NULL, 3, false ),
       (2, 1, '2', NULL, 3, false ),
       (3, 1, '3', NULL, 3, false ),

       (4, 2, '1', 1, 3, true ),
       (5, 2, '2', 1, 3, true ),
       (6, 2, '3', 2, 3, true ),
       (7, 2, '4', 2, 3, true ),
       (8, 2, '5', 3, 3, true ),
       (9, 2, '6', 3, 3, true ),

       (10, 3, '1', 4, 3, true ),
       (11, 3, '2', 4, 3, true ),
       (12, 3, '3', 5, 3, true ),
       (13, 3, '4', 5, 3, true ),
       (14, 3, '5', 5, 3, true ),
       (15, 3, '6', 6, 3, true ),
       (16, 3, '7', 7, 3, true ),
       (17, 3, '8', 7, 3, true ),
       (18, 3, '9', 8, 3, true )
;

INSERT INTO acl_entry
(id, acl_object_identity, ace_order,
 sid, mask, granting, audit_success, audit_failure)
VALUES
--     user_1 has permissions READ_ORGANIZATION, EDIT for organization_1 organization
    (1, 1, 1, 1, 2, true, true, true),
    (2, 1, 2, 1, 32, true, true, true),
--     user_1 has permissions READ_ORGANIZATION for organization_2 organization
    (3, 2, 1, 1, 2, true, true, true),
--     user_1 has permissions READ_PROJECT for organization_1:project_2
    (4, 5, 1, 1, 1, true, true, true),
--     user_1 has permissions READ_PROJECT for organization_2:project_4
    (5, 7, 1, 1, 1, true, true, true),
--     user_1 has permissions READ_ENVIRONMENT for organization_2:project_4:dev
    (6, 17, 1, 1, 1024, true, true, true),

--     user_2 has permissions READ_ORGANIZATION for organization_3
    (7, 3, 1, 2, 2, true, true, true),
--     user_2 has permissions EDIT for organization_3:project_5
    (8, 8, 1, 2, 32, true, true, true)

       ;


TRUNCATE ORGANIZATION CASCADE;
TRUNCATE project CASCADE;
TRUNCATE environment CASCADE;

INSERT INTO ORGANIZATION (ID, ORGANIZATION_NAME) values (1, 'organization_1');
insert into project (id, project_name, organization_id, removed) values (1, 'project_1', 1, false);
INSERT INTO ENVIRONMENT (ID, ENVIRONMENT_NAME, PROJECT_ID, AUTH_KEY_HASH, REMOVED) values (1, 'prod', 1, 'AUTH_KEY_HASH', false);
INSERT INTO ENVIRONMENT (ID, ENVIRONMENT_NAME, PROJECT_ID, AUTH_KEY_HASH, REMOVED) values (2, 'uat', 1, 'AUTH_KEY_HASH', false);
insert into features (id, feat_uid, project_id, sprint, description, feature_group) values (1, 'feature_1', 1, '', '', '');
insert into feature_environment_state (feature_id, environment_id, enable) VALUES (1, 1, true);
insert into feature_environment_state (feature_id, environment_id, enable) VALUES (1, 2, false);

insert into project (id, project_name, organization_id, removed) values (2, 'project_2', 1, false);
INSERT INTO ENVIRONMENT (ID, ENVIRONMENT_NAME, PROJECT_ID, AUTH_KEY_HASH, REMOVED) values (3, 'prod', 2, 'AUTH_KEY_HASH', false);
INSERT INTO ENVIRONMENT (ID, ENVIRONMENT_NAME, PROJECT_ID, AUTH_KEY_HASH, REMOVED) values (4, 'uat', 2, 'AUTH_KEY_HASH', false);
INSERT INTO ENVIRONMENT (ID, ENVIRONMENT_NAME, PROJECT_ID, AUTH_KEY_HASH, REMOVED) values (5, 'qa', 2, 'AUTH_KEY_HASH', false);
insert into features (id, feat_uid, project_id, sprint, description, feature_group) values (2, 'feature_2', 2, '', '', '');
insert into feature_environment_state (feature_id, environment_id, enable) VALUES (2, 3, true);
insert into feature_environment_state (feature_id, environment_id, enable) VALUES (2, 4, false);
insert into feature_environment_state (feature_id, environment_id, enable) VALUES (2, 5, false);

INSERT INTO ORGANIZATION (ID, ORGANIZATION_NAME) values (2, 'organization_2');
insert into project (id, project_name, organization_id, removed) values (3, 'project_3', 2, false);
INSERT INTO ENVIRONMENT (ID, ENVIRONMENT_NAME, PROJECT_ID, AUTH_KEY_HASH, REMOVED) values (6, 'prod', 3, 'AUTH_KEY_HASH', false);
insert into features (id, feat_uid, project_id, sprint, description, feature_group) values (3, 'feature_3', 3, '', '', '');
insert into feature_environment_state (feature_id, environment_id, enable) VALUES (3, 6, true);
insert into project (id, project_name, organization_id, removed) values (4, 'project_4', 2, false);
INSERT INTO ENVIRONMENT (ID, ENVIRONMENT_NAME, PROJECT_ID, AUTH_KEY_HASH, REMOVED) values (7, 'prod', 4, 'AUTH_KEY_HASH', false);
INSERT INTO ENVIRONMENT (ID, ENVIRONMENT_NAME, PROJECT_ID, AUTH_KEY_HASH, REMOVED) values (8, 'dev', 4, 'AUTH_KEY_HASH', false);
insert into features (id, feat_uid, project_id, sprint, description, feature_group) values (4, 'feature_4', 4, '', '', '');
insert into feature_environment_state (feature_id, environment_id, enable) VALUES (4, 7, true);
insert into feature_environment_state (feature_id, environment_id, enable) VALUES (4, 8, false);

INSERT INTO ORGANIZATION (ID, ORGANIZATION_NAME) values (3, 'organization_3');
insert into project (id, project_name, organization_id, removed) values (5, 'project_5', 3, false);
INSERT INTO ENVIRONMENT (ID, ENVIRONMENT_NAME, PROJECT_ID, AUTH_KEY_HASH, REMOVED) values (9, 'dev-1', 5, 'AUTH_KEY_HASH', false);
insert into features (id, feat_uid, project_id, sprint, description, feature_group) values (5, 'feature_5', 5, '', '', '');
insert into feature_environment_state (feature_id, environment_id, enable) VALUES (5, 9, true);

insert into project (id, project_name, organization_id, removed) values (6, 'project_6', 3, false);
