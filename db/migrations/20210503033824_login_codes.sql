-- migrate:up
CREATE TABLE login_codes (
       id UUID PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT NOW(),
       updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
       expires_at TIMESTAMP NOT NULL DEFAULT NOW(),
       code TEXT NOT NULL,
       user_id UUID REFERENCES users(id)
);

CREATE TRIGGER set_login_code_timestamp
BEFORE UPDATE ON login_codes
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

-- migrate:down
DROP TABLE login_codes;
