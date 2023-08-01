TRUNCATE ORGANIZATION CASCADE;
TRUNCATE project CASCADE;
INSERT INTO ORGANIZATION (ID, ORGANIZATION_NAME) values (1, 'organization_1');
INSERT INTO ORGANIZATION (ID, ORGANIZATION_NAME) values (20, 'organization_2');
insert into project (id, project_name, organization_id, removed) values (1, 'Super test project 1', 1, false);
insert into project (id, project_name, organization_id, removed) values (10, 'Super test project 2', 1, true);