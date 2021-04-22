-- migrate:up
alter table bubbles add column name varchar(512);

-- migrate:down
alter table bubbles drop column name;
