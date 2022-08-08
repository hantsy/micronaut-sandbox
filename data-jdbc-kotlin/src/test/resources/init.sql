CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS posts(
id UUID DEFAULT uuid_generate_v4() ,
title VARCHAR(255) NOT NULL,
content VARCHAR(255) NOT NULL,
status VARCHAR(255) NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP 
);

-- CREATE SEQUENCE comments_id_seq;

CREATE TABLE IF NOT EXISTS comments(
-- id INTEGER NOT NULL PRIMARY KEY DEFAULT nextval('comments_id_seq') ,
id UUID DEFAULT uuid_generate_v4() ,
post_id UUID NOT NULL,
content VARCHAR(255) NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP
);

-- ALTER SEQUENCE comments_id_seq OWNED BY comments.id;
-- drop foreign key constraints
ALTER TABLE comments DROP CONSTRAINT IF EXISTS fk_comments_to_posts;
-- drop primary key  constraints
ALTER TABLE comments DROP CONSTRAINT IF EXISTS pk_comments;
ALTER TABLE posts DROP CONSTRAINT IF EXISTS pk_posts;
-- add primary key  constraints
ALTER TABLE posts ADD CONSTRAINT  pk_posts PRIMARY KEY (id);
ALTER TABLE comments ADD CONSTRAINT  pk_comments PRIMARY KEY (id);
--add foreigh key  constraints
ALTER TABLE comments ADD CONSTRAINT  fk_comments_to_posts FOREIGN KEY (post_id) REFERENCES posts (id) /* [jooq ignore start] */ ON DELETE CASCADE/* [jooq ignore stop] */;