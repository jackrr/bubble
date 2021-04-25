-- migrate:up
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE bubbles (
       id UUID PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT NOW(),
       updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TRIGGER set_timestamp
BEFORE UPDATE ON bubbles
FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();


-- migrate:down
DROP TABLE bubbles;
DROP function trigger_set_timestamp;
