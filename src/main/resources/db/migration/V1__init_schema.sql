-- =========================
-- ROLES
-- =========================
CREATE TABLE roles (
                       role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(50) NOT NULL UNIQUE,
                       created_at DATETIME(6),
                       created_by VARCHAR(100),
                       updated_at DATETIME(6),
                       updated_by VARCHAR(100)
);

-- =========================
-- USERS
-- =========================
CREATE TABLE users (
                       user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(100),
                       email VARCHAR(100) NOT NULL UNIQUE,
                       mobile_number VARCHAR(15),
                       password_hash VARCHAR(500) NOT NULL,
                       created_at DATETIME(6),
                       created_by VARCHAR(100),
                       updated_at DATETIME(6),
                       updated_by VARCHAR(100)
);

-- =========================
-- USER ROLES
-- =========================
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,

                            PRIMARY KEY (user_id, role_id),

                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(user_id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id)
                                    REFERENCES roles(role_id)
                                    ON DELETE CASCADE
);

-- =========================
-- ADDRESS
-- =========================
CREATE TABLE address (
                         address_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         city VARCHAR(100),
                         country VARCHAR(100),
                         postal_code VARCHAR(20),
                         state VARCHAR(100),
                         street VARCHAR(150),

                         user_id BIGINT UNIQUE,

                         created_at DATETIME(6),
                         created_by VARCHAR(100),
                         updated_at DATETIME(6),
                         updated_by VARCHAR(100),

                         CONSTRAINT fk_address_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(user_id)
                                 ON DELETE CASCADE
);

-- =========================
-- CONTACTS
-- =========================
CREATE TABLE contacts (
                          contact_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(100),
                          email VARCHAR(100),
                          mobile_number VARCHAR(15),
                          message VARCHAR(500),
                          status VARCHAR(50),

                          created_at DATETIME(6),
                          created_by VARCHAR(100),
                          updated_at DATETIME(6),
                          updated_by VARCHAR(100)
);

-- =========================
-- CATEGORIES
-- =========================
CREATE TABLE categories (
                            category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(100) NOT NULL,
                            description VARCHAR(500),

                            created_at DATETIME(6),
                            created_by VARCHAR(100),
                            updated_at DATETIME(6),
                            updated_by VARCHAR(100)
);

-- =========================
-- PRODUCTS
-- =========================
CREATE TABLE products (
                          product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(250) NOT NULL,
                          description VARCHAR(1000),
                          price DECIMAL(10,2) NOT NULL,

                          category_id BIGINT,

                          created_at DATETIME(6),
                          created_by VARCHAR(100),
                          updated_at DATETIME(6),
                          updated_by VARCHAR(100),

                          CONSTRAINT fk_product_category
                              FOREIGN KEY (category_id)
                                  REFERENCES categories(category_id)
);

-- =========================
-- PRODUCT IMAGES
-- =========================
CREATE TABLE product_image (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               image_url VARCHAR(255),
                               public_id VARCHAR(255),
                               product_id BIGINT,

                               CONSTRAINT fk_product_image_product
                                   FOREIGN KEY (product_id)
                                       REFERENCES products(product_id)
                                       ON DELETE CASCADE
);

-- =========================
-- DISCOUNTS
-- =========================
CREATE TABLE discounts (
                           discount_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           description VARCHAR(255),
                           percentage DECIMAL(5,2),
                           active BIT(1),
                           start_date DATE,
                           end_date DATE,

                           product_id BIGINT,

                           created_at DATETIME(6),
                           created_by VARCHAR(100),
                           updated_at DATETIME(6),
                           updated_by VARCHAR(100),

                           CONSTRAINT fk_discount_product
                               FOREIGN KEY (product_id)
                                   REFERENCES products(product_id)
                                   ON DELETE CASCADE
);

-- =========================
-- CARTS
-- =========================
CREATE TABLE carts (
                       cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                       customer_id BIGINT UNIQUE,

                       created_at DATETIME(6),
                       created_by VARCHAR(100),
                       updated_at DATETIME(6),
                       updated_by VARCHAR(100),

                       CONSTRAINT fk_cart_user
                           FOREIGN KEY (customer_id)
                               REFERENCES users(user_id)
                               ON DELETE CASCADE
);

-- =========================
-- CART ITEMS
-- =========================
CREATE TABLE cart_items (
                            cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                            price DECIMAL(10,2),
                            quantity INT,

                            cart_id BIGINT,
                            product_id BIGINT,

                            created_at DATETIME(6),
                            created_by VARCHAR(100),
                            updated_at DATETIME(6),
                            updated_by VARCHAR(100),

                            CONSTRAINT fk_cart_item_cart
                                FOREIGN KEY (cart_id)
                                    REFERENCES carts(cart_id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_cart_item_product
                                FOREIGN KEY (product_id)
                                    REFERENCES products(product_id)
);

-- =========================
-- ORDERS
-- =========================
CREATE TABLE orders (
                        order_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                        user_id BIGINT,
                        total_price DECIMAL(10,2),

                        created_at DATETIME(6),
                        created_by VARCHAR(100),
                        updated_at DATETIME(6),
                        updated_by VARCHAR(100),

                        CONSTRAINT fk_order_user
                            FOREIGN KEY (user_id)
                                REFERENCES users(user_id)
);

-- =========================
-- ORDER ITEMS
-- =========================
CREATE TABLE order_items (
                             order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                             price DECIMAL(10,2),
                             quantity INT,

                             order_id BIGINT,
                             product_id BIGINT,

                             created_at DATETIME(6),
                             created_by VARCHAR(100),
                             updated_at DATETIME(6),
                             updated_by VARCHAR(100),

                             CONSTRAINT fk_order_item_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(order_id)
                                     ON DELETE CASCADE,

                             CONSTRAINT fk_order_item_product
                                 FOREIGN KEY (product_id)
                                     REFERENCES products(product_id)
);

-- =========================
-- PASSWORD RESET TOKENS
-- =========================
CREATE TABLE password_reset_tokens (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       expiry_date DATETIME(6),
                                       token_hash VARCHAR(255),

                                       user_id BIGINT UNIQUE,

                                       CONSTRAINT fk_token_user
                                           FOREIGN KEY (user_id)
                                               REFERENCES users(user_id)
                                               ON DELETE CASCADE
);

-- =========================
-- SEED DATA
-- =========================
INSERT INTO roles (name) VALUES
                             ('ROLE_USER'),
                             ('ROLE_ADMIN');