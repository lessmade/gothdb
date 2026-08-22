CREATE TABLE friends (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(64) NOT NULL,
    email VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_friends PRIMARY KEY (id),
    CONSTRAINT uq_friends_email UNIQUE (email)
);

CREATE TABLE products (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    sku VARCHAR(32) NOT NULL,
    name VARCHAR(128) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id),
    CONSTRAINT uq_products_sku UNIQUE (sku)
);

CREATE TABLE orders (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    friend_id BIGINT NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT fk_orders_friend FOREIGN KEY (friend_id) REFERENCES friends (id)
);

CREATE INDEX idx_orders_friend_id ON orders (friend_id);

CREATE TABLE order_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
);

CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);
