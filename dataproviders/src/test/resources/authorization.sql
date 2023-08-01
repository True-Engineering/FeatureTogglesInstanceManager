TRUNCATE acl_sid CASCADE;
TRUNCATE acl_class CASCADE;
TRUNCATE acl_object_identity CASCADE;
TRUNCATE acl_entry CASCADE;

INSERT INTO acl_sid (id, principal, sid)
VALUES (1, true, 'user_1'),
       (2, true, 'user_2'),
       (3, false, 'ROLE_FEATURE_FLAGS_ADMIN'),
       (4, true, 'user_3'),
       (5, true, 'user_4');

INSERT INTO acl_class (id, class)
VALUES (1, 'ru.trueengineering.featureflag.manager.core.domen.organization.Organization'),
       (2, 'ru.trueengineering.featureflag.manager.core.domen.project.Project'),
       (3, 'ru.trueengineering.featureflag.manager.core.domen.environment.Environment');
;

INSERT INTO acl_object_identity
(id, object_id_class, object_id_identity,
 parent_object, owner_sid, entries_inheriting)
VALUES (1, 1, '1', NULL, 3, false),
       (2, 1, '2', NULL, 3, false),
       (3, 2, '1', 1, 3, true),
       (4, 2, '2', 1, 3, true),
       (5, 1, '20', NULL, 3, false),
       (6, 3, '1', 4, 3, true);

INSERT INTO acl_entry
(id, acl_object_identity, ace_order,
 sid, mask, granting, audit_success, audit_failure)
VALUES (1, 1, 1, 1, 1, true, true, true),
       (2, 1, 2, 1, 2, true, true, true),
       (3, 1, 3, 2, 1, true, true, true),
       -- user_3 имеет право READ на Project 1
       (4, 3, 4, 4, 1, true, true, true),
       -- user_4 имеет право READ_ORGANIZATION на Organization 1
       (5, 1, 5, 5, 2, true, true, true),
       (6, 6, 1, 1, 1024, true, true, true),
       (7, 1, 6, 3, 32, true, true, true),
       (8, 1, 4, 3, 16, true, true, true)
;