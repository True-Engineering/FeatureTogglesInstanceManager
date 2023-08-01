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
VALUES (1, 'ru.trueengineering.featureflag.manager.core.domen.organization.Organization');

INSERT INTO acl_object_identity
(id, object_id_class, object_id_identity,
 parent_object, owner_sid, entries_inheriting)
VALUES
       (1, 1, '1', NULL, 3, true);

INSERT INTO acl_entry
(id, acl_object_identity, ace_order,
 sid, mask, granting, audit_success, audit_failure)
VALUES
       -- user_5 имеет права на редактирование Organization 1
       (2, 1, 2, 6, 2, true, true, true),
       (6, 1, 6, 6, 16, true, true, true),
       (7, 1, 7, 6, 32, true, true, true),
       (10, 1, 10, 6, 512, true, true, true),

       -- user_4 имеет права на чтение на Project 1
       (14, 1, 1, 5, 2, true, true, true)

;
