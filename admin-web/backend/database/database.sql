-- Tạo database CoffeShop - Cấu trúc cuối cùng
-- Chỉ giữ lại các bảng cần thiết cho MySQL
-- Chạy script này trong phpMyAdmin hoặc MySQL command line

CREATE DATABASE IF NOT EXISTS `CoffeShop` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `CoffeShop`;

-- Xóa các bảng cũ nếu có (để tạo lại)
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `addresses`;
DROP TABLE IF EXISTS `vouchers`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `cart`;
DROP TABLE IF EXISTS `wishlist`;
DROP TABLE IF EXISTS `android_metadata`;

-- ============================================
-- BẢNG BẮT BUỘC - MySQL
-- ============================================

-- Bảng users (BẮT BUỘC)
CREATE TABLE `users` (
  `user_id` VARCHAR(255) PRIMARY KEY,
  `phone_number` VARCHAR(50) NOT NULL,
  `full_name` VARCHAR(255),
  `email` VARCHAR(255),
  `password` VARCHAR(255),
  `avatar_path` TEXT,
  `created_at` BIGINT NOT NULL,
  `is_logged_in` TINYINT(1) DEFAULT 0,
  `auth_token` TEXT,
  `is_admin` TINYINT(1) DEFAULT 0,
  INDEX `idx_phone_number` (`phone_number`),
  INDEX `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng orders (BẮT BUỘC) - Đã loại bỏ items_json
CREATE TABLE `orders` (
  `order_id` VARCHAR(255) PRIMARY KEY,
  `user_id` VARCHAR(255) NOT NULL,
  `total_price` DECIMAL(10,2) NOT NULL,
  `order_date` BIGINT NOT NULL,
  `status` VARCHAR(50) NOT NULL DEFAULT 'pending',
  `delivery_address` TEXT,
  `phone_number` VARCHAR(50),
  `customer_name` VARCHAR(255),
  `payment_method` VARCHAR(100),
  `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_order_date` (`order_date`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng order_items (TẠO MỚI) - Tách từ JSON
CREATE TABLE `order_items` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `order_id` VARCHAR(255) NOT NULL,
  `item_title` VARCHAR(255) NOT NULL,
  `item_json` TEXT NOT NULL,
  `quantity` INT(11) NOT NULL DEFAULT 1,
  `price` DECIMAL(10,2) NOT NULL,
  `subtotal` DECIMAL(10,2) NOT NULL,
  `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  INDEX `idx_order_id` (`order_id`),
  FOREIGN KEY (`order_id`) REFERENCES `orders`(`order_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng vouchers (NÊN chuyển MySQL)
CREATE TABLE `vouchers` (
  `voucher_id` VARCHAR(255) PRIMARY KEY,
  `code` VARCHAR(100) NOT NULL UNIQUE,
  `discount_percent` DECIMAL(5,2) DEFAULT 0,
  `discount_amount` DECIMAL(10,2) DEFAULT 0,
  `discount_type` VARCHAR(20) NOT NULL DEFAULT 'PERCENT',
  `min_order_amount` DECIMAL(10,2) DEFAULT 0,
  `max_discount_amount` DECIMAL(10,2) DEFAULT 0,
  `start_date` BIGINT NOT NULL,
  `end_date` BIGINT NOT NULL,
  `usage_limit` INT(11) DEFAULT 0,
  `used_count` INT(11) DEFAULT 0,
  `is_active` TINYINT(1) DEFAULT 1,
  `description` TEXT,
  `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  INDEX `idx_code` (`code`),
  INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng addresses (NÊN chuyển MySQL)
CREATE TABLE `addresses` (
  `id` VARCHAR(255) PRIMARY KEY,
  `user_id` VARCHAR(255) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(50) NOT NULL,
  `address` TEXT NOT NULL,
  `is_default` TINYINT(1) DEFAULT 0,
  `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  INDEX `idx_user_id` (`user_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- INSERT DỮ LIỆU
-- ============================================

-- Users
INSERT INTO `users` (`user_id`, `phone_number`, `full_name`, `email`, `password`, `avatar_path`, `created_at`, `is_logged_in`, `auth_token`, `is_admin`) VALUES
('1764006966270', 'Facebook_1764006966270', 'Facebook User', 'facebook@example.com', '', '', 1764006966270, 0, NULL, 0),
('1764015318340', '0846230059', 'Nam Hoai', 'nam2612@gmail.com', 'Nam26122005@', '', 1764015318340, 1, NULL, 0),
('1764092999957', '0846230058', '', '', 'Nam26122005@', '', 1764092999957, 0, NULL, 0),
('admin_1764100290009', 'admin', 'Administrator', 'admin@coffeeshop.com', 'admin123', '', 1764100290009, 0, NULL, 1),
('1764149624664', '0359269322', 'Nguyen Anh Thien', 'anhthien@gmail.com', '12345678', '', 1764149624664, 0, NULL, 0);

-- Orders (không có items_json nữa)
INSERT INTO `orders` (`order_id`, `user_id`, `total_price`, `order_date`, `status`, `delivery_address`, `phone_number`, `customer_name`, `payment_method`) VALUES
('30f31a83-36dc-4904-98a8-c521a4c70db5', '1764015318340', 3.80, 1764015571468, 'Processing', '471 Nguyen Van Qua', '0846230059', 'Nam Hoai', 'Tiền mặt'),
('8349d7c0-ea0d-427a-927d-c9ad97c7d477', '1764015318340', 25.80, 1764103436620, 'Processing', '1 HCm', '0846230059', 'Hoai Nam', 'Tiền mặt'),
('3769269d-1eaf-4ca6-9642-5132b0f7f9d2', '1764015318340', 16.10, 1764103780194, 'Pending', 'HCM', '0846230059', 'Hoai Nam', 'Tiền mặt'),
('3c286209-451a-4097-8cc8-d30f83de2fe0', '1764015318340', 4.50, 1764103906422, 'Completed', 'HCM', '0846230059', 'Hoai Nma', 'Tiền mặt'),
('738ec95a-9a35-402a-991a-aa5e53597f17', '1764015318340', 5.80, 1764104252354, 'Completed', 'HCM', '0846230059', 'Hoai Nma', 'Tiền mặt'),
('a5846668-1547-4c8f-94d5-a1b0b1bca3ec', '1764015318340', 4.50, 1764104540644, 'Completed', 'HCM', '0846230059', 'Hoai Nma', 'Tiền mặt'),
('6a1b286f-3a99-408a-a662-f46e8b5e3612', '1764015318340', 4.50, 1764104707460, 'Completed', 'HCM', '0846230059', 'Hoai Nma', 'Tiền mặt'),
('618590f1-fa8b-47f3-ac02-1e6d2775d7fa', '1764015318340', 12.00, 1764152185305, 'Pending', 'HCM', '0846230059', 'Hoai Nam', 'Tiền mặt'),
('89835972-9b6f-446a-989e-c4af62caa1e8', '1764015318340', 31.50, 1764171558605, 'Completed', 'B', '0359269323', 'Nam Ngu', 'Tiền mặt'),
('0503f2d5-6870-4833-819d-ba126e1a921b', '1764015318340', 4.50, 1764377994706, 'Completed', 'HCM', '0846230059', 'Hoai Nma', 'Tiền mặt'),
('205c552a-14a5-42fc-98c3-4bcc4ca35ffb', '1764015318340', 4.50, 1764378314845, 'Completed', 'quận 12', '0955588845', 'Kim Huy', 'Ví điện tử'),
('4e1fd19e-9531-410e-93fe-bfe2fa06b0fe', '1764015318340', 11.60, 1764378724480, 'Pending', 'HCM', '0846230056', 'Anh Tật', 'Tiền mặt'),
('257420c2-4307-4d3b-89a9-a4b3e184449f', '1764015318340', 8.10, 1764379009623, 'Pending', 'HCM', '0846230059', 'Hoai Nam', 'Tiền mặt'),
('f9a47e70-b8e6-4035-9485-eb89d04710d3', '1764015318340', 6.20, 1764379936846, 'Pending', 'Quận 12', '0344474114', 'Kim Ngọc Tiến', 'Tiền mặt'),
('03b24b50-3fce-4ccb-8d21-b363310daf23', '1764015318340', 120.06, 1764381639374, 'Pending', 'HCM', '0846230059', 'Hoai Nam', 'Tiền mặt');

-- Order Items (tách từ JSON của orders cũ)
INSERT INTO `order_items` (`order_id`, `item_title`, `item_json`, `quantity`, `price`, `subtotal`) VALUES
('30f31a83-36dc-4904-98a8-c521a4c70db5', 'Mocha Espresso', '{"categoryId":"0","description":"Rich espresso blended with chocolate for a bold, sweet flavor.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006330/Espresso-_S%E1%BB%AFa_%C4%91en_v%C3%A0_soclola_cafe_dpjbzt.png"],"price":3.8,"rating":0.0,"title":"Mocha Espresso"}', 1, 3.80, 3.80),
('8349d7c0-ea0d-427a-927d-c9ad97c7d477', 'Cappuccino Cocoa', '{"categoryId":"1","description":"Cappuccino topped with rich cocoa powder.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006323/Cappuccino_%C3%9D_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_t%C6%B0%C6%A1i_%C4%91%C3%A1nh_b%E1%BB%8Dt_nhi%E1%BB%81u_truy%E1%BB%81n_th%E1%BB%91ng_yzn4gn.png"],"price":4.3,"rating":0.0,"title":"Cappuccino Cocoa"}', 6, 4.30, 25.80),
('3769269d-1eaf-4ca6-9642-5132b0f7f9d2', 'Blueberry Cream Latte', '{"categoryId":"","description":"Creamy latte topped with blueberry puree.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006355/Latte_-_Tr%C3%A0nh_Xanh_Vi%E1%BB%87t_Qu%E1%BB%91c_v%C3%A0_kem_b%E1%BB%8Dt_soda_iloue7.png"],"price":5.8,"rating":0.0,"title":"Blueberry Cream Latte"}', 2, 5.80, 11.60),
('3769269d-1eaf-4ca6-9642-5132b0f7f9d2', 'Espresso Fusion', '{"categoryId":"","description":"Espresso fusion with creamy layers.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/6_whjo3t.png"],"price":4.5,"rating":0.0,"title":"Espresso Fusion"}', 1, 4.50, 4.50),
('3c286209-451a-4097-8cc8-d30f83de2fe0', 'Espresso Fusion', '{"categoryId":"","description":"Espresso fusion with creamy layers.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/6_whjo3t.png"],"price":4.5,"rating":0.0,"title":"Espresso Fusion"}', 1, 4.50, 4.50),
('738ec95a-9a35-402a-991a-aa5e53597f17', 'Blueberry Cream Latte', '{"categoryId":"","description":"Creamy latte topped with blueberry puree.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006355/Latte_-_Tr%C3%A0nh_Xanh_Vi%E1%BB%87t_Qu%E1%BB%91c_v%C3%A0_kem_b%E1%BB%8Dt_soda_iloue7.png"],"price":5.8,"rating":0.0,"title":"Blueberry Cream Latte"}', 1, 5.80, 5.80),
('a5846668-1547-4c8f-94d5-a1b0b1bca3ec', 'Espresso Fusion', '{"categoryId":"","description":"Espresso fusion with creamy layers.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/6_whjo3t.png"],"price":4.5,"rating":0.0,"title":"Espresso Fusion"}', 1, 4.50, 4.50),
('6a1b286f-3a99-408a-a662-f46e8b5e3612', 'Espresso Fusion', '{"categoryId":"","description":"Espresso fusion with creamy layers.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/6_whjo3t.png"],"price":4.5,"rating":0.0,"title":"Espresso Fusion"}', 1, 4.50, 4.50),
('618590f1-fa8b-47f3-ac02-1e6d2775d7fa', 'Blueberry Cream Latte', '{"categoryId":"","description":"Creamy latte topped with blueberry puree.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006355/Latte_-_Tr%C3%A0nh_Xanh_Vi%E1%BB%87t_Qu%E1%BB%91c_v%C3%A0_kem_b%E1%BB%8Dt_soda_iloue7.png"],"price":5.8,"rating":0.0,"title":"Blueberry Cream Latte"}', 1, 5.80, 5.80),
('89835972-9b6f-446a-989e-c4af62caa1e8', 'Espresso Fusion', '{"categoryId":"","description":"Espresso fusion with creamy layers.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/6_whjo3t.png"],"price":4.5,"rating":0.0,"title":"Espresso Fusion"}', 7, 4.50, 31.50),
('0503f2d5-6870-4833-819d-ba126e1a921b', 'Espresso Fusion', '{"categoryId":"","description":"Espresso fusion with creamy layers.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/6_whjo3t.png"],"price":4.5,"rating":0.0,"title":"Espresso Fusion"}', 1, 4.50, 4.50),
('205c552a-14a5-42fc-98c3-4bcc4ca35ffb', 'Espresso Fusion', '{"categoryId":"","description":"Espresso fusion with creamy layers.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/6_whjo3t.png"],"price":4.5,"rating":0.0,"title":"Espresso Fusion"}', 1, 4.50, 4.50),
('4e1fd19e-9531-410e-93fe-bfe2fa06b0fe', 'Blueberry Cream Latte', '{"categoryId":"","description":"Creamy latte topped with blueberry puree.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006355/Latte_-_Tr%C3%A0nh_Xanh_Vi%E1%BB%87t_Qu%E1%BB%91c_v%C3%A0_kem_b%E1%BB%8Dt_soda_iloue7.png"],"price":5.8,"rating":0.0,"title":"Blueberry Cream Latte"}', 2, 5.80, 11.60),
('257420c2-4307-4d3b-89a9-a4b3e184449f', 'Espresso Fusion', '{"categoryId":"","description":"Espresso fusion with creamy layers.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/6_whjo3t.png"],"price":4.5,"rating":0.0,"title":"Espresso Fusion"}', 2, 4.50, 8.10),
('f9a47e70-b8e6-4035-9485-eb89d04710d3', 'Matcha Bear Cream Latte', '{"categoryId":"","description":"Matcha latte topped with bear whipped cream.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006371/Latte_-_Latte_v%E1%BB%8B_truy%E1%BB%81n_th%E1%BB%91ng_v%C3%A0_kem_nhi%E1%BB%81u_nxz8h1.png"],"price":6.2,"rating":0.0,"title":"Matcha Bear Cream Latte"}', 1, 6.20, 6.20),
('03b24b50-3fce-4ccb-8d21-b363310daf23', 'Blueberry Cream Latte', '{"categoryId":"","description":"Creamy latte topped with blueberry puree.","extra":"","numberInCart":0,"picUrl":["https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006355/Latte_-_Tr%C3%A0nh_Xanh_Vi%E1%BB%87t_Qu%E1%BB%91c_v%C3%A0_kem_b%E1%BB%8Dt_soda_iloue7.png"],"price":5.8,"rating":0.0,"title":"Blueberry Cream Latte"}', 23, 5.80, 120.06);

-- Vouchers
INSERT INTO `vouchers` (`voucher_id`, `code`, `discount_percent`, `discount_amount`, `discount_type`, `min_order_amount`, `max_discount_amount`, `start_date`, `end_date`, `usage_limit`, `used_count`, `is_active`, `description`) VALUES
('voucher_1764378976251_1', 'WELCOME10', 10.00, 0.00, 'PERCENT', 0.00, 50000.00, 1764378976250, 1795914976250, 0, 2, 1, 'Giảm 10% cho đơn hàng đầu tiên, tối đa 50.000 đ'),
('voucher_1764378976251_2', 'SAVE20', 20.00, 0.00, 'PERCENT', 200000.00, 100000.00, 1764378976250, 1795914976250, 0, 0, 1, 'Giảm 20% cho đơn hàng từ 200.000 đ, tối đa 100.000 đ'),
('voucher_1764378976251_3', 'FLASH50K', 0.00, 50000.00, 'AMOUNT', 300000.00, 0.00, 1764378976250, 1795914976250, 100, 0, 1, 'Giảm 50.000 đ cho đơn hàng từ 300.000 đ'),
('voucher_1764378976251_4', 'NEWYEAR30', 30.00, 0.00, 'PERCENT', 100000.00, 0.00, 1764378976250, 1795914976250, 50, 0, 1, 'Giảm 30% cho đơn hàng từ 100.000 đ'),
('voucher_1764378976251_5', 'FREE25K', 0.00, 25000.00, 'AMOUNT', 50000.00, 0.00, 1764378976250, 1795914976250, 0, 0, 1, 'Giảm 25.000 đ cho đơn hàng từ 50.000 đ');

-- Addresses
INSERT INTO `addresses` (`id`, `user_id`, `name`, `phone`, `address`, `is_default`) VALUES
('f439854c-1841-491b-b139-ad8b9f0e42ad', 'admin_1764100290009', 'Hoai Nam', '0846230059', 'HCM', 0),
('9911a515-2f2a-49e9-852f-7a2999287188', 'admin_1764100290009', 'Hoai Nma', '0846230059', 'HCM', 0);

