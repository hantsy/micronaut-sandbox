CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS posts
(
    id         UUID         NOT NULL DEFAULT uuid_generate_v4(),
    title      VARCHAR(255) NOT NULL,
    content    VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT LOCALTIMESTAMP
);

ALTER TABLE posts
    DROP CONSTRAINT IF EXISTS pk_posts;
ALTER TABLE posts
    ADD CONSTRAINT pk_posts PRIMARY KEY (id);