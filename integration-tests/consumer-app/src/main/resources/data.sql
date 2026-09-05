INSERT INTO friends (username, email) VALUES
    ('alice', 'alice@chanes.in'),
    ('marilyn', 'marilyn@manson.com'),
    ('emo', 'emo@core.com');

INSERT INTO products (sku, name, price) VALUES
    ('SKU-001', 'Obsidian Bat Pendant', 18.50),
    ('SKU-002', 'Ceremonial Athame', 42.00),
    ('SKU-003', 'Rick Owens Socks', 129.90),
    ('SKU-004', 'New Rock Tnk Boots', 15.75),
    ('SKU-005', 'Schector Tempest Guitar', 349.00);

INSERT INTO orders (friend_id, status) VALUES
    (1, 'COMPLETED'),
    (1, 'PENDING'),
    (2, 'COMPLETED'),
    (3, 'CANCELLED');

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
    (1, 1, 1, 18.50),
    (1, 4, 2, 15.75),
    (2, 3, 1, 129.90),
    (3, 5, 1, 349.00),
    (3, 2, 1, 42.00),
    (4, 1, 1, 18.50);
