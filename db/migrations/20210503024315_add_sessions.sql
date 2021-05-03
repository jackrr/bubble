-- migrate:up
CREATE TABLE sessions (
       id UUID PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT NOW(),
       updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
       expires_at TIMESTAMP NOT NULL DEFAULT NOW(),
       user_id UUID REFERENCES users(id)
);

CREATE TRIGGER set_session_timestamp
BEFORE UPDATE ON sessions
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

-- migrate:down
DROP TABLE sessions;
