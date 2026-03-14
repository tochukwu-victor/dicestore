-- =========================
-- PRODUCT STOCK AND OPTIMISTIC LOCKING
-- =========================
ALTER TABLE products ADD COLUMN stock INT NOT NULL DEFAULT 0;
ALTER TABLE products ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- =========================
-- ORDER OPTIMISTIC LOCKING
-- =========================
ALTER TABLE orders ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- =========================
-- CART OPTIMISTIC LOCKING
-- =========================
ALTER TABLE carts ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
