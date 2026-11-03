CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS customers(
    id UUID DEFAULT uuid_generate_v4() ,
    name VARCHAR(255) NOT NULL,
    age INTEGER NOT NULL DEFAULT '0',
    street VARCHAR(255),
    city VARCHAR(255),
    zip VARCHAR(255),
    version BIGINT NOT NULL DEFAULT '0'
    );

ALTER TABLE customers DROP CONSTRAINT IF EXISTS pk_customers;
ALTER TABLE customers ADD CONSTRAINT  pk_customers PRIMARY KEY (id);
