-- Migration: Add tracking columns to orders table and create order_tracking table
-- Date: 2024-12-01

-- Add payment_method and updated_at columns to orders table
ALTER TABLE orders
ADD COLUMN payment_method VARCHAR(50) DEFAULT 'COD',
ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Create order_tracking table with tracking info
CREATE TABLE IF NOT EXISTS order_tracking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    note TEXT,
    tracking_number VARCHAR(100),
    shipping_partner VARCHAR(100),
    estimated_delivery DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_tracking_number (tracking_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comment
ALTER TABLE order_tracking COMMENT = 'Tracks order status changes and shipping events';

