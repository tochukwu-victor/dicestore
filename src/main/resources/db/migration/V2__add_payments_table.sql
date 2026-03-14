-- =========================
-- ADD MISSING ORDER STATUS COLUMN
-- (was missing from V1 orders table)
-- =========================
ALTER TABLE orders ADD COLUMN order_status VARCHAR(50) NOT NULL DEFAULT 'PENDING_PAYMENT';

-- =========================
-- PAYMENTS
-- =========================
CREATE TABLE payments (
                          payment_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                          order_id            BIGINT NOT NULL UNIQUE,
                          paystack_reference  VARCHAR(100) NOT NULL UNIQUE,
                          status              VARCHAR(20) NOT NULL,
                          amount              DECIMAL(10,2) NOT NULL,
                          payment_method      VARCHAR(30),
                          currency            VARCHAR(10) DEFAULT 'NGN',
                          gateway_response    TEXT,
                          paid_at             DATETIME(6),

                          created_at          DATETIME(6),
                          created_by          VARCHAR(100),
                          updated_at          DATETIME(6),
                          updated_by          VARCHAR(100),

                          CONSTRAINT fk_payment_order
                              FOREIGN KEY (order_id)
                                  REFERENCES orders(order_id)
                                  ON DELETE CASCADE,

                          INDEX idx_payment_reference (paystack_reference)
);