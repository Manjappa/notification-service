-- Create database (run this manually if needed)
-- CREATE DATABASE notification_db;

-- Create payment_details table
CREATE TABLE IF NOT EXISTS payment_details (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    merchant_email VARCHAR(255) NOT NULL,
    merchant_name VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    payment_method VARCHAR(255) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(500),
    customer_email VARCHAR(255),
    customer_name VARCHAR(255),
    transaction_date TIMESTAMP,
    order_id VARCHAR(255),
    description VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT chk_payment_status CHECK (payment_status IN ('SUCCESS', 'FAILED'))
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_transaction_id ON payment_details(transaction_id);
CREATE INDEX IF NOT EXISTS idx_merchant_email ON payment_details(merchant_email);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payment_details(payment_status);
CREATE INDEX IF NOT EXISTS idx_created_at ON payment_details(created_at);

