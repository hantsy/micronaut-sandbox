CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS customers(
    id UUID NOT NULL /* [jooq ignore start] */DEFAULT uuid_generate_v4()/* [jooq ignore stop] */,
    name VARCHAR(255) NOT NULL,
    age INTEGER NOT NULL DEFAULT 0,
    street VARCHAR(255),
    city VARCHAR(255),
    zip VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS orders(
    id UUID NOT NULL /* [jooq ignore start] */ DEFAULT uuid_generate_v4()/* [jooq ignore stop] */,
    customer_id UUID NOT NULL,
    amount NUMERIC(12,2) DEFAULT '0.0',
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items(
    id UUID NOT NULL /* [jooq ignore start] */ DEFAULT uuid_generate_v4()/* [jooq ignore stop] */,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS products(
    id UUID NOT NULL /* [jooq ignore start] */ DEFAULT uuid_generate_v4()/* [jooq ignore stop] */,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(12,2) DEFAULT '0.0'
);

-- drop fk constraints
ALTER TABLE orders DROP CONSTRAINT IF EXISTS fk_orders_customers;
ALTER TABLE order_items DROP CONSTRAINT IF EXISTS fk_items_orders;
ALTER TABLE order_items DROP CONSTRAINT IF EXISTS fk_items_products;

-- drop primary keys
ALTER TABLE customers DROP CONSTRAINT IF EXISTS pk_customers;
ALTER TABLE orders DROP CONSTRAINT IF EXISTS pk_orders;
ALTER TABLE order_items DROP CONSTRAINT IF EXISTS pk_order_items;
ALTER TABLE products DROP CONSTRAINT IF EXISTS pk_products;

-- add primary keys
ALTER TABLE customers ADD CONSTRAINT  pk_customers PRIMARY KEY (id);
ALTER TABLE orders ADD CONSTRAINT  pk_orders PRIMARY KEY (id);
ALTER TABLE order_items ADD CONSTRAINT  pk_order_items PRIMARY KEY (id);
ALTER TABLE products ADD CONSTRAINT  pk_products PRIMARY KEY (id);

-- add fk constraints
ALTER TABLE orders ADD CONSTRAINT fk_orders_customers FOREIGN KEY (customer_id) REFERENCES customers(id) /* [jooq ignore start] */ON DELETE CASCADE ON UPDATE CASCADE/* [jooq ignore stop] */;
ALTER TABLE order_items ADD CONSTRAINT fk_items_orders FOREIGN KEY (order_id) REFERENCES orders(id) /* [jooq ignore start] */ON UPDATE CASCADE/* [jooq ignore stop] */;
ALTER TABLE order_items ADD CONSTRAINT fk_items_products FOREIGN KEY (product_id) REFERENCES products(id) /* [jooq ignore start] */ON UPDATE CASCADE/* [jooq ignore stop] */;


