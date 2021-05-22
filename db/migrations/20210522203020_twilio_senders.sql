-- migrate:up
create table senders (
       id UUID PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT NOW(),
       updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
       phone TEXT
);

CREATE TRIGGER set_sms_numbers_timestamp
BEFORE UPDATE ON senders
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

-- This was only safe because it is not deployed.
DELETE FROM bubbles_users;

alter table bubbles_users add column sender_id UUID NOT NULL REFERENCES senders(id);

CREATE UNIQUE INDEX bu_users_bubbles_idx ON bubbles_users (bubble_id, user_id);
CREATE UNIQUE INDEX bu_users_senders_idx ON bubbles_users (user_id, sender_id);

-- migrate:down
drop index bu_users_senders_idx;
drop index bu_users_bubbles_idx;
alter table bubbles_users drop column sender_id;
drop table senders;
