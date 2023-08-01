TRUNCATE acl_sid CASCADE;
TRUNCATE acl_class CASCADE;
TRUNCATE acl_object_identity CASCADE;
TRUNCATE acl_entry CASCADE;

INSERT INTO acl_sid (id, principal, sid)
VALUES
    (3, false, 'FEATURE_FLAGS_ADMIN'),
        -- user с правами только на чтение
       (5, true, 'user_4'),
       -- user с правами только на изменение
       (6, true, 'user_5'),
       -- user без прав
       (7, true, 'user_6');

INSERT INTO acl_class (id, class)
VALUES (1, 'ru.trueengineering.featureflag.manager.core.domen.organization.Organization'),
    (2, 'ru.trueengineering.featureflag.manager.core.domen.project.Project');

INSERT INTO acl_object_identity
(id, object_id_class, object_id_identity,
 parent_object, owner_sid, entries_inheriting)
VALUES (1, 2, '1', NULL, 3, false),
       (2, 1, '1', NULL, 3, true);

INSERT INTO acl_entry
(id, acl_object_identity, ace_order,
 sid, mask, granting, audit_success, audit_failure)
VALUES
       -- user_5 имеет права на редактирование Project 1
       (1, 2, 1, 6, 32, true, true, true),
       (2, 1, 2, 6, 2, true, true, true),
       (3, 1, 3, 6, 4, true, true, true),
       (4, 1, 4, 6, 8, true, true, true),
       (6, 1, 6, 6, 32, true, true, true),
       (7, 1, 7, 6, 64, true, true, true),
       (8, 1, 8, 6, 128, true, true, true),
       (9, 1, 9, 6, 256, true, true, true),
       (10, 1, 10, 6, 512, true, true, true),
       (11, 1, 11, 6, 1024, true, true, true),

       -- user_4 имеет права на чтение на Project 1
       (12, 1, 12, 5, 1, true, true, true),
       (13, 1, 13, 5, 4, true, true, true),
       (14, 2, 2, 5, 2, true, true, true)

;
