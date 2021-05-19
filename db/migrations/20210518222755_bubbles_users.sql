-- migrate:up
create table bubbles_users (
       id UUID PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT NOW(),
       updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
       bubble_id UUID NOT NULL REFERENCES bubbles(id),
       user_id UUID NOT NULL REFERENCES users(id)
);

CREATE TRIGGER set_bubbles_users_timestamp
BEFORE UPDATE ON bubbles_users
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

-- migrate:down
drop table bubbles_users;
