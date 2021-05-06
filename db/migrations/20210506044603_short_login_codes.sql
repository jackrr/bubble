-- migrate:up
alter table login_codes add column short boolean;
alter table login_codes add column short_code TEXT;

-- migrate:down
alter table login_codes drop column short;
alter table login_codes drop column short_code;
