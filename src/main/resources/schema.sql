
-- =========================
-- Table: contacts
-- =========================
CREATE TABLE contacts (
    contact_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL,
    mobile_number   VARCHAR(15),
    message         VARCHAR(500),
    status          VARCHAR(50),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by      VARCHAR(20) NOT NULL DEFAULT 'system',
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(20) DEFAULT 'system'
);

-- =========================
-- Table: roles
-- =========================
CREATE TABLE roles (
    role_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by  VARCHAR(20) NOT NULL DEFAULT 'system',
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by  VARCHAR(20) DEFAULT 'system'
);


-- Insert initial roles
INSERT INTO roles (name) VALUES
    ('ROLE_USER'),
    ('ROLE_ADMIN'),
    ('ROLE_OPS_ENG'),
    ('ROLE_QA_ENG');


-- =========================
-- Table: customers
-- =========================
CREATE TABLE customers (
    customer_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    mobile_number   VARCHAR(15) NOT NULL UNIQUE,
    password_hash   VARCHAR(500) NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by      VARCHAR(20) NOT NULL DEFAULT 'system',
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(20) DEFAULT 'system'
);

-- =========================
-- Table: customer_roles (junction table)
-- =========================
CREATE TABLE customer_roles (
    customer_id BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,
    PRIMARY KEY (customer_id, role_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);

-- =========================
-- Table: products
-- =========================
CREATE TABLE products (
    product_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(250) NOT NULL,
    description  VARCHAR(500),
    price        DECIMAL(10,2) NOT NULL,
    popularity   INT DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by   VARCHAR(20) NOT NULL DEFAULT 'system',
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by   VARCHAR(20) DEFAULT 'system'
);

CREATE TABLE discounts (
    discount_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    percentage DECIMAL(5, 2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    active TINYINT(1) DEFAULT 1 NOT NULL,
    start_date TIMESTAMP NULL,
    end_date TIMESTAMP NULL,
    product_id BIGINT NOT NULL,
    -- BaseEntity fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(20) NOT NULL DEFAULT 'system',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(20) DEFAULT 'system',
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- =========================
-- Table: product_image
-- =========================
CREATE TABLE product_image (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    public_id   VARCHAR(255) NOT NULL,
    url         VARCHAR(255) NOT NULL,
    product_id  BIGINT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- =========================
-- Table: carts
-- =========================
CREATE TABLE carts (
    cart_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by  VARCHAR(20) NOT NULL DEFAULT 'system',
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by  VARCHAR(20) DEFAULT 'system',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- =========================
-- Table: cart_items
-- =========================
CREATE TABLE cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id      BIGINT NOT NULL,
    product_id   BIGINT NOT NULL,
    quantity     INT NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by   VARCHAR(20) NOT NULL DEFAULT 'system',
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by   VARCHAR(20) DEFAULT 'system',
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- =========================
-- Table: orders
-- =========================
CREATE TABLE orders (
    order_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id    BIGINT NOT NULL,
    total_price    DECIMAL(10,2) NOT NULL,
    payment_id     VARCHAR(200),
    payment_status VARCHAR(50),
    order_status   VARCHAR(50),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by     VARCHAR(20) NOT NULL DEFAULT 'system',
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by     VARCHAR(20) DEFAULT 'system',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- =========================
-- Table: order_items
-- =========================
CREATE TABLE order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT NOT NULL,
    product_id    BIGINT NOT NULL,
    quantity      INT NOT NULL,
    price         DECIMAL(10,2) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by    VARCHAR(20) NOT NULL DEFAULT 'system',
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by    VARCHAR(20) DEFAULT 'system',
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- =========================
-- Table: address
-- =========================
CREATE TABLE address (
    address_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    street      VARCHAR(150),
    city        VARCHAR(100),
    state       VARCHAR(100),
    postal_code VARCHAR(20),
    country     VARCHAR(100),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by  VARCHAR(20) NOT NULL DEFAULT 'system',
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by  VARCHAR(20) DEFAULT 'system',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);
