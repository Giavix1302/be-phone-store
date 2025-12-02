-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               11.7.2-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             12.10.0.7000
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Dumping database structure for phone_ecommerce
CREATE DATABASE IF NOT EXISTS `phone_ecommerce` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `phone_ecommerce`;

-- Dumping structure for procedure phone_ecommerce.AddToCart
DELIMITER //
CREATE PROCEDURE `AddToCart`(
    IN p_user_id BIGINT,
    IN p_product_id BIGINT,
    IN p_color_id BIGINT,
    IN p_quantity INT
)
BEGIN
    DECLARE v_cart_id BIGINT;
    DECLARE v_current_price DECIMAL(10,2);
    DECLARE v_existing_quantity INT DEFAULT 0;
    
    -- Get or create cart for user
    SELECT id INTO v_cart_id FROM cart WHERE user_id = p_user_id;
    
    IF v_cart_id IS NULL THEN
        INSERT INTO cart (user_id) VALUES (p_user_id);
        SET v_cart_id = LAST_INSERT_ID();
    END IF;
    
    -- Get current product price
    SELECT COALESCE(discount_price, price) INTO v_current_price 
    FROM products WHERE id = p_product_id;
    
    -- Check if item already exists in cart
    SELECT quantity INTO v_existing_quantity 
    FROM cart_items 
    WHERE cart_id = v_cart_id AND product_id = p_product_id AND color_id = p_color_id;
    
    IF v_existing_quantity > 0 THEN
        -- Update existing item
        UPDATE cart_items 
        SET quantity = quantity + p_quantity,
            unit_price = v_current_price
        WHERE cart_id = v_cart_id AND product_id = p_product_id AND color_id = p_color_id;
    ELSE
        -- Insert new item
        INSERT INTO cart_items (cart_id, product_id, color_id, quantity, unit_price)
        VALUES (v_cart_id, p_product_id, p_color_id, p_quantity, v_current_price);
    END IF;
    
END//
DELIMITER ;

-- Dumping structure for table phone_ecommerce.brands
CREATE TABLE IF NOT EXISTS `brands` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  KEY `idx_brand_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table phone_ecommerce.brands: ~10 rows (approximately)
INSERT INTO `brands` (`id`, `name`, `description`, `created_at`) VALUES
	(1, 'Apple', 'Thương hiệu công nghệ từ Mỹ', '2025-11-12 08:09:50'),
	(2, 'Samsung', 'Tập đoàn công nghệ Hàn Quốc', '2025-11-12 08:09:50'),
	(3, 'Xiaomi', 'Thương hiệu công nghệ Trung Quốc', '2025-11-12 08:09:50'),
	(4, 'Oppo', 'Thương hiệu điện thoại Trung Quốc', '2025-11-12 08:09:50'),
	(5, 'Vivo', 'Thương hiệu điện thoại Trung Quốc', '2025-11-12 08:09:50'),
	(6, 'Huawei', 'Tập đoàn công nghệ Trung Quốc', '2025-11-12 08:09:50'),
	(7, 'Dell', 'Thương hiệu máy tính Mỹ', '2025-11-12 08:09:50'),
	(8, 'HP', 'Thương hiệu máy tính Mỹ', '2025-11-12 08:09:50'),
	(9, 'Asus', 'Thương hiệu máy tính Đài Loan', '2025-11-12 08:09:50'),
	(10, 'Lenovo', 'Thương hiệu máy tính Trung Quốc', '2025-11-12 08:09:50');

-- Dumping structure for table phone_ecommerce.cart
CREATE TABLE IF NOT EXISTS `cart` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_cart_user` (`user_id`),
  CONSTRAINT `cart_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table phone_ecommerce.cart: ~0 rows (approximately)
INSERT INTO `cart` (`id`, `user_id`, `created_at`) VALUES
	(1, 6, '2025-11-14 06:31:37');

-- Dumping structure for table phone_ecommerce.cart_items
CREATE TABLE IF NOT EXISTS `cart_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cart_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `color_id` bigint(20) NOT NULL COMMENT 'User selected color',
  `quantity` int(11) NOT NULL DEFAULT 1,
  `unit_price` decimal(10,2) NOT NULL COMMENT 'Price snapshot',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_cart_product_color` (`cart_id`,`product_id`,`color_id`),
  KEY `idx_cart_items_cart` (`cart_id`),
  KEY `idx_cart_items_product` (`product_id`),
  KEY `idx_cart_items_color` (`color_id`),
  CONSTRAINT `cart_items_ibfk_1` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`id`) ON DELETE CASCADE,
  CONSTRAINT `cart_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  CONSTRAINT `cart_items_ibfk_3` FOREIGN KEY (`color_id`) REFERENCES `colors` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table phone_ecommerce.cart_items: ~0 rows (approximately)

-- Dumping structure for table phone_ecommerce.categories
CREATE TABLE IF NOT EXISTS `categories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_category_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table phone_ecommerce.categories: ~3 rows (approximately)
INSERT INTO `categories` (`id`, `name`, `description`, `created_at`, `updated_at`) VALUES
	(1, 'PHONE', 'Điện thoại thông minh', '2025-11-12 08:09:50', NULL),
	(2, 'TABLET', 'Máy tính bảng', '2025-11-12 08:09:50', NULL),
	(3, 'LAPTOP', 'Máy tính xách tay', '2025-11-12 08:09:50', NULL);

-- Dumping structure for table phone_ecommerce.colors
CREATE TABLE IF NOT EXISTS `colors` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `color_name` varchar(50) NOT NULL,
  `hex_code` varchar(7) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_color_name` (`color_name`),
  KEY `idx_hex_code` (`hex_code`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table phone_ecommerce.colors: ~8 rows (approximately)
INSERT INTO `colors` (`id`, `color_name`, `hex_code`, `created_at`, `updated_at`) VALUES
	(1, 'Đen', '#000000', '0000-00-00 00:00:00.000000', NULL),
	(2, 'Trắng', '#FFFFFF', '0000-00-00 00:00:00.000000', NULL),
	(3, 'Xanh Navy', '#1E3A8A', '0000-00-00 00:00:00.000000', NULL),
	(4, 'Hồng', '#EC4899', '0000-00-00 00:00:00.000000', NULL),
	(5, 'Xanh Lá', '#10B981', '0000-00-00 00:00:00.000000', NULL),
	(6, 'Vàng', '#F59E0B', '0000-00-00 00:00:00.000000', NULL),
	(7, 'Tím', '#8B5CF6', '0000-00-00 00:00:00.000000', NULL),
	(8, 'Xám', '#6B7280', '0000-00-00 00:00:00.000000', NULL);

-- Dumping structure for procedure phone_ecommerce.GetCartTotal
DELIMITER //
CREATE PROCEDURE `GetCartTotal`(
    IN p_user_id BIGINT,
    OUT p_total DECIMAL(10,2)
)
BEGIN
    SELECT COALESCE(SUM(ci.quantity * ci.unit_price), 0) INTO p_total
    FROM cart c
    JOIN cart_items ci ON c.id = ci.cart_id
    WHERE c.user_id = p_user_id;
END//
DELIMITER ;

-- Dumping structure for table phone_ecommerce.orders
CREATE TABLE IF NOT EXISTS `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `order_number` varchar(255) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `status` enum('PENDING','PROCESSING','SHIPPED','DELIVERED','CANCELLED') DEFAULT 'PENDING',
  `shipping_address` text NOT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `notes` text DEFAULT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_number` (`order_number`),
  KEY `idx_order_user` (`user_id`),
  KEY `idx_order_number` (`order_number`),
  KEY `idx_order_status` (`status`),
  KEY `idx_order_date` (`created_at`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table phone_ecommerce.orders: ~4 rows (approximately)
INSERT INTO `orders` (`id`, `user_id`, `order_number`, `total_amount`, `status`, `shipping_address`, `created_at`, `notes`, `payment_method`, `updated_at`) VALUES
	(1, 6, 'ORD20251114150022WVT', 23000000.00, 'PROCESSING', '123 Đường ABC, Phường XYZ, Quận 1, TP.HCM', '2025-11-14 08:00:22', 'Giao hàng giờ hành chính\nĐơn hàng đã được xác nhận và đang chuẩn bị', NULL, NULL),
	(2, 6, 'ORD20251114150422CXI', 15000000.00, 'CANCELLED', '123 Đường ABC, Phường XYZ, Quận 1, TP.HCM', '2025-11-14 08:04:22', 'Giao hàng giờ hành chính\nCancel reason: Thay đổi ý định mua hàng', NULL, NULL),
	(3, 6, 'ORD20251114150509TGC', 15000000.00, 'SHIPPED', '123 Đường ABC, Phường XYZ, Quận 1, TP.HCM', '2025-11-14 08:05:09', 'Giao hàng giờ hành chính', NULL, NULL),
	(4, 6, 'ORD20251114185806UYY', 23000000.00, 'DELIVERED', '123 Đường ABC, Phường XYZ, Quận 1, TP.HCM', '2025-11-14 11:58:06', 'Giao hàng giờ hành chính\nĐơn hàng đã được xác nhận và đang chuẩn bị\nĐơn hàng đã được xác nhận và đang chuẩn bị\nĐơn hàng đã được xác nhận và đang chuẩn bị\nĐơn hàng đã được xác nhận và đang chuẩn bị\nĐơn hàng đã được xác nhận và đang chuẩn bị', 'COD', '2025-11-14 19:10:20.232620');

-- Dumping structure for table phone_ecommerce.order_items
CREATE TABLE IF NOT EXISTS `order_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `quantity` int(11) NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  `color_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_items_order` (`order_id`),
  KEY `idx_order_items_product` (`product_id`),
  KEY `FKq4wvof0qrtimcy2gpqpist101` (`color_id`),
  CONSTRAINT `FKq4wvof0qrtimcy2gpqpist101` FOREIGN KEY (`color_id`) REFERENCES `colors` (`id`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table phone_ecommerce.order_items: ~4 rows (approximately)
INSERT INTO `order_items` (`id`, `order_id`, `product_id`, `quantity`, `unit_price`, `color_id`) VALUES
	(1, 1, 1, 1, 23000000.00, 1),
	(2, 2, 3, 1, 15000000.00, 1),
	(3, 3, 3, 1, 15000000.00, 1),
	(4, 4, 1, 1, 23000000.00, 1);

-- Dumping structure for table phone_ecommerce.order_tracking
CREATE TABLE IF NOT EXISTS `order_tracking` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` text DEFAULT NULL,
  `estimated_delivery` datetime(6) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `note` text DEFAULT NULL,
  `shipping_partner` varchar(100) DEFAULT NULL,
  `status` enum('CANCELLED','DELIVERED','PENDING','PROCESSING','SHIPPED') NOT NULL,
  `tracking_number` varchar(100) DEFAULT NULL,
  `order_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKeu0lumcx8bcx6lk035xiklty0` (`order_id`),
  CONSTRAINT `FKeu0lumcx8bcx6lk035xiklty0` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table phone_ecommerce.order_tracking: ~4 rows (approximately)
INSERT INTO `order_tracking` (`id`, `created_at`, `description`, `estimated_delivery`, `location`, `note`, `shipping_partner`, `status`, `tracking_number`, `order_id`) VALUES
	(1, '2025-11-14 18:58:06.224057', 'Đơn hàng đã được tạo', NULL, '', NULL, NULL, 'PENDING', NULL, 4),
	(2, '2025-11-14 19:01:12.657832', 'Đơn hàng đang được chuẩn bị', '2024-12-05 17:00:00.000000', '', 'Đơn hàng đã được xác nhận và đang chuẩn bị', 'Giao Hàng Nhanh', 'PROCESSING', 'GHN123456789', 4),
	(3, '2025-11-14 19:03:07.742101', 'Đơn hàng đã được giao cho đối tác vận chuyển', '2024-12-05 17:00:00.000000', '', 'Đơn hàng đã được xác nhận và đang chuẩn bị', 'Giao Hàng Nhanh', 'SHIPPED', 'GHN123456789', 4),
	(6, '2025-11-14 19:10:20.223595', 'Đơn hàng đã giao thành công', '2024-12-05 17:00:00.000000', 'TPHCM', 'Đơn hàng đã được xác nhận và đang chuẩn bị', 'Giao Hàng Nhanh', 'DELIVERED', 'GHN123456789', 4);

-- Dumping structure for table phone_ecommerce.products
CREATE TABLE IF NOT EXISTS `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `slug` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `price` decimal(10,2) NOT NULL,
  `discount_price` decimal(10,2) DEFAULT NULL,
  `stock_quantity` int(11) DEFAULT 0,
  `category_id` bigint(20) NOT NULL,
  `brand_id` bigint(20) NOT NULL,
  `color_id` bigint(20) NOT NULL COMMENT 'Default color',
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `slug` (`slug`),
  KEY `color_id` (`color_id`),
  KEY `idx_product_name` (`name`),
  KEY `idx_product_slug` (`slug`),
  KEY `idx_product_price` (`price`),
  KEY `idx_product_category` (`category_id`),
  KEY `idx_product_brand` (`brand_id`),
  KEY `idx_product_active` (`is_active`),
  KEY `idx_product_stock` (`stock_quantity`),
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `products_ibfk_2` FOREIGN KEY (`brand_id`) REFERENCES `brands` (`id`),
  CONSTRAINT `products_ibfk_3` FOREIGN KEY (`color_id`) REFERENCES `colors` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table phone_ecommerce.products: ~5 rows (approximately)
INSERT INTO `products` (`id`, `name`, `slug`, `description`, `price`, `discount_price`, `stock_quantity`, `category_id`, `brand_id`, `color_id`, `is_active`, `created_at`) VALUES
	(1, 'iPhone 15 Pro', 'iphone-15-pro', 'Điện thoại iPhone 15 Pro với chip A17 Pro', 25000000.00, 23000000.00, 49, 1, 1, 1, 1, '2025-11-12 08:09:50'),
	(2, 'Samsung Galaxy S24', 'samsung-galaxy-s24', 'Điện thoại Samsung Galaxy S24 với AI', 20000000.00, 18000000.00, 30, 1, 2, 1, 1, '2025-11-12 08:09:50'),
	(3, 'Xiaomi 14', 'xiaomi-14', 'Điện thoại Xiaomi 14 với camera Leica', 15000000.00, NULL, 24, 1, 3, 2, 1, '2025-11-12 08:09:50'),
	(4, 'iPad Pro 12.9', 'ipad-pro-12-9', 'Máy tính bảng iPad Pro 12.9 inch', 30000000.00, NULL, 50, 2, 1, 3, 1, '2025-11-12 08:09:50'),
	(5, 'MacBook Air M3', 'macbook-air-m3', 'Laptop MacBook Air với chip M3', 35000000.00, 32000000.00, 15, 3, 1, 3, 1, '2025-11-12 08:09:50');

-- Dumping structure for view phone_ecommerce.product_available_colors
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `product_available_colors` (
	`product_id` BIGINT(20) NOT NULL,
	`product_name` VARCHAR(1) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`color_id` BIGINT(20) NOT NULL,
	`color_name` VARCHAR(1) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`hex_code` VARCHAR(1) NOT NULL COLLATE 'utf8mb4_unicode_ci'
) ENGINE=MyISAM;

-- Dumping structure for table phone_ecommerce.product_colors
CREATE TABLE IF NOT EXISTS `product_colors` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `color_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_product_color` (`product_id`,`color_id`),
  KEY `idx_product_colors_product` (`product_id`),
  KEY `idx_product_colors_color` (`color_id`),
  CONSTRAINT `product_colors_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  CONSTRAINT `product_colors_ibfk_2` FOREIGN KEY (`color_id`) REFERENCES `colors` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table phone_ecommerce.product_colors: ~17 rows (approximately)
INSERT INTO `product_colors` (`id`, `product_id`, `color_id`) VALUES
	(1, 1, 1),
	(2, 1, 2),
	(3, 1, 3),
	(4, 1, 4),
	(5, 2, 1),
	(6, 2, 2),
	(7, 2, 5),
	(8, 2, 6),
	(9, 3, 1),
	(10, 3, 2),
	(11, 3, 7),
	(12, 4, 1),
	(13, 4, 2),
	(14, 4, 3),
	(15, 5, 1),
	(16, 5, 2),
	(17, 5, 8);

-- Dumping structure for view phone_ecommerce.product_details
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `product_details` (
	`id` BIGINT(20) NOT NULL,
	`name` VARCHAR(1) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`slug` VARCHAR(1) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`description` TEXT NULL COLLATE 'utf8mb4_unicode_ci',
	`price` DECIMAL(10,2) NOT NULL,
	`discount_price` DECIMAL(10,2) NULL,
	`stock_quantity` INT(11) NULL,
	`category_name` VARCHAR(1) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`brand_name` VARCHAR(1) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`default_color` VARCHAR(1) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`default_hex_code` VARCHAR(1) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`is_active` TINYINT(1) NULL,
	`created_at` TIMESTAMP NULL
) ENGINE=MyISAM;

-- Dumping structure for table phone_ecommerce.product_images
CREATE TABLE IF NOT EXISTS `product_images` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `image_url` varchar(500) NOT NULL,
  `alt_text` varchar(255) DEFAULT NULL,
  `is_primary` tinyint(1) DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_image_product` (`product_id`),
  KEY `idx_image_primary` (`is_primary`),
  CONSTRAINT `product_images_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table phone_ecommerce.product_images: ~7 rows (approximately)
INSERT INTO `product_images` (`id`, `product_id`, `image_url`, `alt_text`, `is_primary`) VALUES
	(1, 1, '/images/iphone-15-pro-black-1.jpg', 'iPhone 15 Pro màu đen', 1),
	(2, 1, '/images/iphone-15-pro-black-2.jpg', 'iPhone 15 Pro màu đen mặt sau', 0),
	(3, 2, '/images/samsung-s24-black-1.jpg', 'Samsung Galaxy S24 màu đen', 1),
	(4, 2, '/images/samsung-s24-black-2.jpg', 'Samsung Galaxy S24 màu đen mặt sau', 0),
	(5, 3, '/images/xiaomi-14-white-1.jpg', 'Xiaomi 14 màu trắng', 1),
	(6, 4, '/images/ipad-pro-blue-1.jpg', 'iPad Pro màu xanh navy', 1),
	(7, 5, '/images/macbook-air-blue-1.jpg', 'MacBook Air màu xanh navy', 1);

-- Dumping structure for table phone_ecommerce.product_specifications
CREATE TABLE IF NOT EXISTS `product_specifications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `spec_name` varchar(255) NOT NULL,
  `spec_value` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_spec_product` (`product_id`),
  KEY `idx_spec_name` (`spec_name`),
  KEY `idx_spec_value` (`spec_value`),
  KEY `idx_spec_name_value` (`spec_name`,`spec_value`),
  CONSTRAINT `product_specifications_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table phone_ecommerce.product_specifications: ~35 rows (approximately)
INSERT INTO `product_specifications` (`id`, `product_id`, `spec_name`, `spec_value`) VALUES
	(1, 1, 'Screen Size', '6.1 inch'),
	(2, 1, 'RAM', '8GB'),
	(3, 1, 'Storage', '256GB'),
	(4, 1, 'Battery', '3274mAh'),
	(5, 1, 'Camera', '48MP'),
	(6, 1, 'OS', 'iOS 17'),
	(7, 1, 'Chip', 'A17 Pro'),
	(8, 2, 'Screen Size', '6.2 inch'),
	(9, 2, 'RAM', '8GB'),
	(10, 2, 'Storage', '256GB'),
	(11, 2, 'Battery', '4000mAh'),
	(12, 2, 'Camera', '50MP'),
	(13, 2, 'OS', 'Android 14'),
	(14, 2, 'Chip', 'Snapdragon 8 Gen 3'),
	(15, 3, 'Screen Size', '6.36 inch'),
	(16, 3, 'RAM', '12GB'),
	(17, 3, 'Storage', '256GB'),
	(18, 3, 'Battery', '4610mAh'),
	(19, 3, 'Camera', '50MP Leica'),
	(20, 3, 'OS', 'MIUI 15'),
	(21, 3, 'Chip', 'Snapdragon 8 Gen 3'),
	(22, 4, 'Screen Size', '12.9 inch'),
	(23, 4, 'RAM', '8GB'),
	(24, 4, 'Storage', '256GB'),
	(25, 4, 'Battery', '10000mAh'),
	(26, 4, 'Camera', '12MP'),
	(27, 4, 'OS', 'iPadOS 17'),
	(28, 4, 'Chip', 'M2'),
	(29, 5, 'Screen Size', '13.6 inch'),
	(30, 5, 'RAM', '8GB'),
	(31, 5, 'Storage', '256GB SSD'),
	(32, 5, 'Battery', '52.6Wh'),
	(33, 5, 'Camera', '1080p FaceTime'),
	(34, 5, 'OS', 'macOS Sonoma'),
	(35, 5, 'Chip', 'Apple M3');

-- Dumping structure for view phone_ecommerce.product_specs_view
-- Creating temporary table to overcome VIEW dependency errors
CREATE TABLE `product_specs_view` (
	`product_id` BIGINT(20) NOT NULL,
	`product_name` VARCHAR(1) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`specifications` MEDIUMTEXT NULL COLLATE 'utf8mb4_unicode_ci'
) ENGINE=MyISAM;

-- Dumping structure for table phone_ecommerce.reviews
CREATE TABLE IF NOT EXISTS `reviews` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `rating` int(11) NOT NULL CHECK (`rating` >= 1 and `rating` <= 5),
  `comment` text DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_product_review` (`user_id`,`product_id`),
  KEY `idx_review_user` (`user_id`),
  KEY `idx_review_product` (`product_id`),
  KEY `idx_review_rating` (`rating`),
  KEY `idx_review_date` (`created_at`),
  CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table phone_ecommerce.reviews: ~1 rows (approximately)
INSERT INTO `reviews` (`id`, `user_id`, `product_id`, `rating`, `comment`, `created_at`, `updated_at`) VALUES
	(10, 6, 1, 5, 'Sau thời gian sử dụng, tôi thấy sản phẩm khá ổn nhưng pin có hơi yếu. Thiết kế đẹp, camera tốt.', '2025-11-24 15:05:36', '2025-11-24 22:05:47.209258');

-- Dumping structure for table phone_ecommerce.spring_session
CREATE TABLE IF NOT EXISTS `spring_session` (
  `PRIMARY_ID` char(36) NOT NULL,
  `SESSION_ID` char(36) NOT NULL,
  `CREATION_TIME` bigint(20) NOT NULL,
  `LAST_ACCESS_TIME` bigint(20) NOT NULL,
  `MAX_INACTIVE_INTERVAL` int(11) NOT NULL,
  `EXPIRY_TIME` bigint(20) NOT NULL,
  `PRINCIPAL_NAME` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`PRIMARY_ID`),
  UNIQUE KEY `SPRING_SESSION_IX1` (`SESSION_ID`),
  KEY `SPRING_SESSION_IX2` (`EXPIRY_TIME`),
  KEY `SPRING_SESSION_IX3` (`PRINCIPAL_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- Dumping data for table phone_ecommerce.spring_session: ~0 rows (approximately)

-- Dumping structure for table phone_ecommerce.spring_session_attributes
CREATE TABLE IF NOT EXISTS `spring_session_attributes` (
  `SESSION_PRIMARY_ID` char(36) NOT NULL,
  `ATTRIBUTE_NAME` varchar(200) NOT NULL,
  `ATTRIBUTE_BYTES` blob NOT NULL,
  PRIMARY KEY (`SESSION_PRIMARY_ID`,`ATTRIBUTE_NAME`),
  CONSTRAINT `SPRING_SESSION_ATTRIBUTES_FK` FOREIGN KEY (`SESSION_PRIMARY_ID`) REFERENCES `spring_session` (`PRIMARY_ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- Dumping data for table phone_ecommerce.spring_session_attributes: ~0 rows (approximately)

-- Dumping structure for table phone_ecommerce.users
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `role` enum('USER','ADMIN') DEFAULT 'USER',
  `created_at` timestamp NULL DEFAULT current_timestamp(),
  `enabled` tinyint(1) DEFAULT 1,
  `avatar` text DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dumping data for table phone_ecommerce.users: ~8 rows (approximately)
INSERT INTO `users` (`id`, `username`, `email`, `password`, `full_name`, `phone`, `address`, `role`, `created_at`, `enabled`, `avatar`, `updated_at`) VALUES
	(1, 'admin', 'admin@phonestore.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9blloyNTOUxjGM6', 'Quản trị viên', NULL, '123 Nguyễn Huệ, Quận 1, TP.HCM', 'ADMIN', '2025-11-12 08:09:50', 1, NULL, NULL),
	(2, 'user', 'user@phonestore.com', '$2a$10$Gdi/.6HXP5YVbmoxTlfnLOYSvEMo33lW4Eprgw/1436flldQOllqu', 'Test User', '0987654321', '456 User Street, Ho Chi Minh City', 'USER', '2025-11-13 01:14:09', 1, NULL, NULL),
	(3, 'user1', 'user1@example.com', '$2a$10$jkVc2nll3MKKIovsnJ0qsOd5c/NVbqU2dAUVF4SUdmT8BnlVTXWkO', 'Nguyễn Văn A', '0123456789', '123 Đường ABC, Quận 1, TP.HCM', 'USER', '2025-11-13 01:40:25', 1, NULL, NULL),
	(4, 'user.test', 'user.test@example.com', '$2a$10$yWWvy2xRy3y5bbUcpZ4RsebZYA6xYM7OBzyfhWAmCtEWf1PPndUsW', 'Nguyễn Văn A', '0123456789', '123 Đường ABC, Quận 1, TP.HCM', 'USER', '2025-11-13 01:44:18', 0, NULL, NULL),
	(5, 'user.test2', 'user.test2@example.com', '$2a$10$NJtMSCn0HwFmcMhUFKFEqeWBl/m54Mb4omHkqTrPOkBQW2OiX6GkS', 'Nguyễn Văn A', '0123456789', '123 Đường ABC, Quận 1, TP.HCM', 'USER', '2025-11-13 01:47:27', 1, NULL, NULL),
	(6, 'user.test3', 'user.test3@example.com', '$2a$10$uzOdN1NtKGF27gtzRgtW5ejfhf.MjCNMD8ChGXqc/eqOE19M6tTH.', 'Nguyễn Văn A Updated', '0111222334', '456 Đường DEF, Phường ABC, Quận 2, TP.HCM', 'USER', '2025-11-14 02:12:57', 1, NULL, '2025-11-14 10:14:07.177722'),
	(7, 'admin2', 'admin2@admin.com', '$2a$10$rgTuPFXgrjUvIsM8ijvIfeb20BxUN1p1InnlR64cixseBr1z83ULm', 'Nguyễn Văn User3', '0123456789', '123 Đường ABC, Quận 1, TP.HCM', 'ADMIN', '2025-11-14 09:03:39', 1, NULL, '2025-11-16 11:10:29.228058'),
	(13, 'quynhnhu18121812', 'quynhnhu18121812@gmail.com', '$2a$10$FXdZUlGp9tsM3PvTO6uu/uYfsJDvPEirQkZRO2DSrDrpUdiea4yFa', 'Nguyễn Văn User33434', '0123456789', '123 Đường ABC, Quận 1, TP.HCM', 'USER', '2025-11-15 13:09:12', 1, 'https://res.cloudinary.com/dkihq7ht3/image/upload/v1763266697/phone-ecommerce/avatars/file_a4ys1a.jpg', '2025-11-16 11:18:18.065004');

-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `product_available_colors`;
CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `product_available_colors` AS SELECT 
    p.id AS product_id,
    p.name AS product_name,
    c.id AS color_id,
    c.color_name,
    c.hex_code
FROM products p
JOIN product_colors pc ON p.id = pc.product_id
JOIN colors c ON pc.color_id = c.id 
;

-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `product_details`;
CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `product_details` AS SELECT 
    p.id,
    p.name,
    p.slug,
    p.description,
    p.price,
    p.discount_price,
    p.stock_quantity,
    c.name AS category_name,
    b.name AS brand_name,
    col.color_name AS default_color,
    col.hex_code AS default_hex_code,
    p.is_active,
    p.created_at
FROM products p
JOIN categories c ON p.category_id = c.id
JOIN brands b ON p.brand_id = b.id
JOIN colors col ON p.color_id = col.id 
;

-- Removing temporary table and create final VIEW structure
DROP TABLE IF EXISTS `product_specs_view`;
CREATE ALGORITHM=UNDEFINED SQL SECURITY DEFINER VIEW `product_specs_view` AS SELECT 
    ps.product_id,
    p.name AS product_name,
    GROUP_CONCAT(
        CONCAT(ps.spec_name, ': ', ps.spec_value) 
        SEPARATOR ', '
    ) AS specifications
FROM product_specifications ps
JOIN products p ON ps.product_id = p.id
GROUP BY ps.product_id, p.name 
;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
