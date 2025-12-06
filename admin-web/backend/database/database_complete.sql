-- =====================================================
-- DATABASE HOÀN CHỈNH CHO COFFEE SHOP MANAGEMENT SYSTEM
-- =====================================================
-- File này tạo toàn bộ database từ đầu đến cuối
-- Bao gồm: Database, Tables, Indexes, Foreign Keys, và Sample Data
-- =====================================================

-- Tạo database (nếu chưa có)
CREATE DATABASE IF NOT EXISTS `CoffeShop` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `CoffeShop`;

-- =====================================================
-- PHẦN 1: XÓA CÁC BẢNG CŨ (NẾU CÓ)
-- =====================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `vouchers`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `banners`;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- PHẦN 2: TẠO CÁC BẢNG
-- =====================================================

-- Bảng Categories
CREATE TABLE `categories` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `imageUrl` VARCHAR(500),
  `isActive` TINYINT(1) DEFAULT 1,
  `createdAt` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updatedAt` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  INDEX `idx_name` (`name`),
  INDEX `idx_isActive` (`isActive`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Products
CREATE TABLE `products` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `price` DECIMAL(10,2) NOT NULL,
  `originalPrice` DECIMAL(10,2),
  `imageUrl` VARCHAR(500),
  `stock` INT(11) DEFAULT 0,
  `isActive` TINYINT(1) DEFAULT 1,
  `categoryId` INT(11),
  `createdAt` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updatedAt` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  INDEX `idx_name` (`name`),
  INDEX `idx_categoryId` (`categoryId`),
  INDEX `idx_isActive` (`isActive`),
  INDEX `idx_stock` (`stock`),
  FOREIGN KEY (`categoryId`) REFERENCES `categories`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Users
CREATE TABLE `users` (
  `user_id` VARCHAR(255) NOT NULL,
  `phone_number` VARCHAR(20) NOT NULL,
  `full_name` VARCHAR(255),
  `email` VARCHAR(255),
  `password` VARCHAR(255),
  `avatar_path` VARCHAR(500),
  `created_at` BIGINT NOT NULL,
  `is_logged_in` TINYINT(1) DEFAULT 0,
  `auth_token` VARCHAR(500),
  `is_admin` TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `idx_phone_number` (`phone_number`),
  INDEX `idx_email` (`email`),
  INDEX `idx_is_admin` (`is_admin`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Vouchers
CREATE TABLE `vouchers` (
  `voucher_id` VARCHAR(255) NOT NULL,
  `code` VARCHAR(50) NOT NULL,
  `discount_percent` DECIMAL(5,2) DEFAULT 0,
  `discount_amount` DECIMAL(10,2) DEFAULT 0,
  `discount_type` VARCHAR(20) DEFAULT 'PERCENT',
  `min_order_amount` DECIMAL(10,2),
  `max_discount_amount` DECIMAL(10,2),
  `start_date` BIGINT NOT NULL,
  `end_date` BIGINT NOT NULL,
  `usage_limit` INT(11) DEFAULT 0,
  `used_count` INT(11) DEFAULT 0,
  `is_active` TINYINT(1) DEFAULT 1,
  `description` TEXT,
  PRIMARY KEY (`voucher_id`),
  UNIQUE KEY `idx_code` (`code`),
  INDEX `idx_discount_type` (`discount_type`),
  INDEX `idx_is_active` (`is_active`),
  INDEX `idx_start_date` (`start_date`),
  INDEX `idx_end_date` (`end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Orders
CREATE TABLE `orders` (
  `order_id` VARCHAR(255) NOT NULL,
  `user_id` VARCHAR(255) NOT NULL,
  `total_price` DECIMAL(10,2) NOT NULL,
  `order_date` BIGINT NOT NULL,
  `status` VARCHAR(50) NOT NULL,
  `delivery_address` VARCHAR(500),
  `phone_number` VARCHAR(20),
  `customer_name` VARCHAR(255),
  `payment_method` VARCHAR(50),
  PRIMARY KEY (`order_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_order_date` (`order_date`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Order Items
CREATE TABLE `order_items` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `order_id` VARCHAR(255) NOT NULL,
  `item_title` VARCHAR(255) NOT NULL,
  `item_json` TEXT NOT NULL,
  `quantity` INT(11) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `subtotal` DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_order_id` (`order_id`),
  FOREIGN KEY (`order_id`) REFERENCES `orders`(`order_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng Banners
CREATE TABLE `banners` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `imageUrl` VARCHAR(500) NOT NULL,
  `linkUrl` VARCHAR(500),
  `displayOrder` INT(11) DEFAULT 0,
  `isActive` TINYINT(1) DEFAULT 1,
  `createdAt` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `updatedAt` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`),
  INDEX `idx_displayOrder` (`displayOrder`),
  INDEX `idx_isActive` (`isActive`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- PHẦN 3: INSERT DỮ LIỆU MẪU
-- =====================================================

-- Insert Categories
INSERT INTO `categories` (`id`, `name`, `description`, `imageUrl`, `isActive`) VALUES
(1, 'Esspersso', 'Espresso coffee drinks with rich, bold flavors', NULL, 1),
(2, 'Cappuccino', 'Classic cappuccino with steamed milk and foam', NULL, 1),
(3, 'Latte', 'Smooth latte drinks with various flavors', NULL, 1),
(4, 'Americano', 'American-style coffee drinks', NULL, 1),
(5, 'Hot Chocolate', 'Rich and creamy hot chocolate beverages', NULL, 1);

-- Insert Products
INSERT INTO `products` (`name`, `description`, `price`, `originalPrice`, `imageUrl`, `stock`, `isActive`, `categoryId`) VALUES
-- Category 1: Esspersso
('Mocha Espresso', 'Rich espresso blended with chocolate for a bold, sweet flavor.', 3.80, 3.80, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006330/Espresso-_S%E1%BB%AFa_%C4%91en_v%C3%A0_soclola_cafe_dpjbzt.png', 50, 1, 1),
('Citrus Espresso', 'Espresso with a hint of citrus peel for a bright aftertaste.', 3.90, 3.90, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006330/Espresso-_cafe_v%C3%A0_v%E1%BB%8B_cam_raxdee.png', 45, 1, 1),
('Light Japanese Espresso', 'Smooth, clean espresso with a gentle roast profile.', 3.50, 3.50, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006331/Espresso-c%C3%A0_ph%C3%AA_xay_m%E1%BB%8Bn_v%C3%A0_n%C6%B0%E1%BB%9Bc_n%C3%B3ng_d%C6%B0%E1%BB%9Bi_%C3%A1p_su%E1%BA%A5t_cao_eukxgy.png', 60, 1, 1),
('Classic Espresso Shot', 'Traditional, full-bodied espresso with strong aroma.', 3.20, 3.20, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006363/Espresso-Cafe_nh%E1%BA%A1t_-_s%E1%BB%AFa_v%E1%BB%ABa_klidtm.png', 70, 1, 1),
('Ground Coffee Espresso', 'Deep-roasted espresso made from finely ground beans.', 3.40, 3.40, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006331/Espresso-c%C3%A0_ph%C3%AA_xay_m%E1%BB%8Bn_v%C3%A0_n%C6%B0%E1%BB%9Bc_n%C3%B3ng_d%C6%B0%E1%BB%9Bi_%C3%A1p_su%E1%BA%A5t_cao_eukxgy.png', 55, 1, 1),
('Dark Roast Espresso', 'Bold, intense espresso with caramelized notes.', 3.60, 3.60, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006326/Espresso_-_Cafe_h%E1%BA%A1t_v%C3%A0_%C4%90%C3%A1nh_b%E1%BB%8Dt_nhi%E1%BB%81u_k5mtfw.png', 40, 1, 1),
('Caramel Art Espresso', 'Espresso topped with light caramel heart art.', 3.80, 3.80, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006322/Espresso_-_Cafe_b%E1%BB%99t_v%C3%A0_l%E1%BB%9Bp_crema_b%E1%BB%8Dt_c%C3%A0_ph%C3%AA_v%C3%A0_h%C3%ACnh_tr%C3%A1i_tim_b%E1%BB%8Dt_aabgpm.png', 50, 1, 1),
('Espresso Fusion', 'Espresso fusion with creamy layers.', 4.50, 4.50, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/6_whjo3t.png', 35, 1, 1),

-- Category 2: Cappuccino
('Cappuccino Cocoa', 'Cappuccino topped with rich cocoa powder.', 4.30, 4.30, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006323/Cappuccino_%C3%9D_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_t%C6%B0%C6%A1i_%C4%91%C3%A1nh_b%E1%BB%8Dt_nhi%E1%BB%81u_truy%E1%BB%81n_th%E1%BB%91ng_yzn4gn.png', 50, 1, 2),
('Italian Cappuccino', 'Authentic Italian cappuccino topped with chocolate dust.', 4.50, 4.50, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006316/Cappuccino_Thi%C3%A9n_nga_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_t%C6%B0%C6%A1i_t%E1%BA%A1o_h%C3%ACnh_rnpfyo.png', 45, 1, 2),
('Animal Art Cappuccino', 'Cappuccino with cute animal latte art.', 4.80, 4.80, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006314/Cappuccino_t%E1%BA%A1o_h%C3%ACnh_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_t%C6%B0%C6%A1i_t%E1%BA%A1o_h%C3%ACnh_theo_%C3%BD_mu%E1%BB%91n_gqmn9x.png', 40, 1, 2),
('Heart Art Cappuccino', 'Cappuccino topped with heart latte art.', 4.70, 4.70, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006314/Cappuccino_t%E1%BA%A1o_h%C3%ACnh_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_t%C6%B0%C6%A1i_t%E1%BA%A1o_h%C3%ACnh_theo_%C3%BD_mu%E1%BB%91n_gqmn9x.png', 50, 1, 2),
('Swan Art Cappuccino', 'Beautiful swan latte art on creamy cappuccino.', 5.00, 5.00, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006314/Cappuccino_ca_cao_-_C%C3%A0_ph%C3%AA_s%E1%BB%AFa_-_B%E1%BB%99t_ca_cao_sthuya.png', 35, 1, 2),
('Traditional Cappuccino', 'Balanced cappuccino with smooth foam.', 4.20, 4.20, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034282/10_l53vw4.png', 60, 1, 2),
('Leaf Art Cappuccino', 'Cappuccino with classic leaf latte art.', 4.40, 4.40, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034282/1_xrreub.png', 55, 1, 2),

-- Category 3: Latte
('Matcha Latte Leaf Art', 'Premium matcha with steamed milk & latte art.', 4.90, 4.90, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/9_ra7zu2.png', 50, 1, 3),
('Classic Matcha Latte', 'Smooth matcha with a creamy and balanced profile.', 4.70, 4.70, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006352/Latte_-_Tr%C3%A0_xanh_v%C3%A0_s%E1%BB%AFa_%C4%91%E1%BA%ADm_b%C3%A9o_v%C3%A0_t%E1%BA%A1o_h%C3%ACnh_xeidvp.png', 55, 1, 3),
('Strong Matcha Latte', 'Deep green, bold matcha for tea lovers.', 4.80, 4.80, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006353/Latte_-_Tr%C3%A0_xanh_Vi%E1%BB%87t_Nam_v%C3%A0_b%E1%BB%99t_latte_s%E1%BB%AFa_v%C3%A0_s%E1%BB%AFa_m%E1%BB%8Fng_tr%C3%A9n_b%E1%BB%81_m%E1%BA%B7t_b2epim.png', 45, 1, 3),
('Vietnamese Matcha Latte', 'Traditional Vietnamese-style matcha with milk foam.', 4.60, 4.60, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006343/Latte_-_Tr%C3%A0_xanh_%C4%91%E1%BA%ADm_v%C3%A0_s%E1%BB%AFa_nh%E1%BA%A1t_sor0gj.png', 50, 1, 3),
('Matcha Latte with Pearls', 'Creamy matcha topped with chewy boba pearls.', 5.50, 5.50, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006353/Latte_-_Tr%C3%A0_xanh_Vi%E1%BB%87t_Nam_v%C3%A0_b%E1%BB%99t_latte_s%E1%BB%AFa_v%C3%A0_s%E1%BB%AFa_m%E1%BB%8Fng_tr%C3%A9n_b%E1%BB%81_m%E1%BA%B7t_b2epim.png', 40, 1, 3),
('3-Layer Matcha Latte', 'Matcha, milk & cream layered beautifully.', 5.20, 5.20, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006353/Latte_-_Tr%C3%A0_xanh_Vi%E1%BB%87t_Nam_v%C3%A0_b%E1%BB%99t_latte_s%E1%BB%AFa_v%C3%A0_s%E1%BB%AFa_m%E1%BB%8Fng_tr%C3%A9n_b%E1%BB%81_m%E1%BA%B7t_b2epim.png', 35, 1, 3),
('Classic Latte Foam', 'Smooth latte with thick milk foam.', 4.50, 4.50, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006339/Latte_-_B%E1%BB%99t_tr%C3%A0_xanh_nhi%E1%BB%85n_v%C3%A0_Ch%C3%A2n_Ch%C3%A2u_%C4%90en_kxe5l2.png', 60, 1, 3),
('Blueberry Cream Latte', 'Creamy latte topped with blueberry puree.', 5.80, 5.80, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006355/Latte_-_Tr%C3%A0nh_Xanh_Vi%E1%BB%87t_Qu%E1%BB%91c_v%C3%A0_kem_b%E1%BB%8Dt_soda_iloue7.png', 30, 1, 3),
('Matcha Bear Cream Latte', 'Matcha latte topped with bear whipped cream.', 6.20, 6.20, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006371/Latte_-_Latte_v%E1%BB%8B_truy%E1%BB%81n_th%E1%BB%91ng_v%C3%A0_kem_nhi%E1%BB%81u_nxz8h1.png', 25, 1, 3),
('Brown Sugar Latte', 'Milk & espresso topped with brown sugar drizzle.', 5.40, 5.40, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006341/Latte_-_S%E1%BB%AFa_kem_v%C3%A0_tr%C3%A0_xanh_nh%E1%BA%ADt_b%E1%BA%A3n_zmuobu.png', 50, 1, 3),
('Milk Cream Matcha Latte', 'Matcha latte topped with milk cream & syrup.', 5.60, 5.60, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006341/Latte_-_Latte_%C3%9D_v%E1%BB%8B_3_l%E1%BB%9Bp_f9vo3s.png', 45, 1, 3),
('Cherry Cream Latte', 'Cherry latte topped with whipped cream & syrup.', 5.90, 5.90, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006365/Latte_-_Latte_v%E1%BB%8B_d%C3%A2u_v%C3%A0_g%E1%BA%A5u_t%E1%BA%A1o_h%C3%ACnh_kem_ov9aob.png', 35, 1, 3),
('Strawberry Cheese Latte', 'Strawberry–cream latte with cheese foam.', 6.00, 6.00, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764080423/Latte_-_Latt_v%E1%BB%8B_cherry_v%C3%A0_%E1%BB%91ng_qu%E1%BA%BF_nhi%E1%BB%81u_kem_1_sv8xb3.png', 30, 1, 3),

-- Category 4: Americano
('Classic Americano', 'Black coffee brewed from premium roasted beans.', 3.20, 3.20, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006309/Americano_Tr%E1%BB%A9ng_-_C%C3%A0_ph%C3%AA_v%C3%A0_l%E1%BB%9Bp_tr%E1%BB%A9ng_m%E1%BB%8Fng_gbx30b.png', 70, 1, 4),
('Iced Americano', 'Refreshing iced americano with a bold finish.', 3.40, 3.40, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006309/Americano_Tr%E1%BA%AFng_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_t%C6%B0%C6%A1i_ux9yrk.png', 65, 1, 4),
('Egg Americano', 'Americano topped with fluffy egg cream.', 4.20, 4.20, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006307/Americano_Red_Eye_-_C%C3%A0_ph%C3%AA_n%C3%B3ng_%E1%BB%A7_l%C3%A2u_says57.png', 50, 1, 4),
('White Americano', 'Light iced americano with milk splash.', 3.70, 3.70, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006307/Americano_Tr%E1%BA%AFng_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_t%C6%B0%C6%A1i_ux9yrk.png', 55, 1, 4),
('Orange Americano', 'Citrus-infused iced americano with orange zest.', 4.00, 4.00, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006306/Americano_%C4%91%C3%A1_-_C%C3%A0_ph%C3%AA_v%C3%A0_n%C6%B0%E1%BB%9Bc_%C4%91%C3%A1_udyxfa.png', 45, 1, 4),
('Mint Americano', 'Iced americano with fresh mint.', 4.10, 4.10, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006309/Americano_B%E1%BA%A1c_H%C3%A0_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_pha_b%E1%BA%A1c_h%C3%A0_cu9etb.png', 50, 1, 4),
('Red Eye Americano', 'Americano with an extra espresso shot.', 4.50, 4.50, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006304/Americano_-_C%C3%A0_ph%C3%AA_v%C3%A0_n%C6%B0%E1%BB%9Bc_n%C3%B3ng_t2rlkf.png', 40, 1, 4),
('Cream Americano', 'Americano topped with silky whipped cream.', 4.30, 4.30, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006308/2Americano_kem_tr%E1%BB%A9ng_-_C%C3%A0_ph%C3%AA_v%C3%A0_tr%E1%BB%A9ng_1_x1lxrf.png', 45, 1, 4);

-- Insert Users (Admin và Sample Users)
INSERT INTO `users` (`user_id`, `phone_number`, `full_name`, `email`, `password`, `avatar_path`, `created_at`, `is_logged_in`, `auth_token`, `is_admin`) VALUES
('user_admin_001', '0123456789', 'Admin User', 'admin@coffeeshop.com', '$2b$10$rQ8K8K8K8K8K8K8K8K8K8O8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K8K', NULL, UNIX_TIMESTAMP(NOW()) * 1000, 0, NULL, 1),
('user_001', '0987654321', 'Nguyễn Văn A', 'nguyenvana@example.com', NULL, NULL, UNIX_TIMESTAMP(NOW()) * 1000, 0, NULL, 0),
('user_002', '0912345678', 'Trần Thị B', 'tranthib@example.com', NULL, NULL, UNIX_TIMESTAMP(NOW()) * 1000, 0, NULL, 0),
('user_003', '0923456789', 'Lê Văn C', 'levanc@example.com', NULL, NULL, UNIX_TIMESTAMP(NOW()) * 1000, 0, NULL, 0);

-- Insert Vouchers
INSERT INTO `vouchers` (`voucher_id`, `code`, `discount_percent`, `discount_amount`, `discount_type`, `min_order_amount`, `max_discount_amount`, `start_date`, `end_date`, `usage_limit`, `used_count`, `is_active`, `description`) VALUES
('voucher_001', 'WELCOME10', 10.00, 0.00, 'PERCENT', 0.00, 50000.00, UNIX_TIMESTAMP('2024-01-01 00:00:00') * 1000, UNIX_TIMESTAMP('2025-12-31 23:59:59') * 1000, 100, 5, 1, 'Mã giảm giá chào mừng - Giảm 10% cho đơn hàng đầu tiên'),
('voucher_002', 'SAVE20', 20.00, 0.00, 'PERCENT', 100000.00, 100000.00, UNIX_TIMESTAMP('2024-01-01 00:00:00') * 1000, UNIX_TIMESTAMP('2025-12-31 23:59:59') * 1000, 50, 2, 1, 'Giảm 20% cho đơn hàng từ 100.000đ'),
('voucher_003', 'FIXED50K', 0.00, 50000.00, 'AMOUNT', 200000.00, 50000.00, UNIX_TIMESTAMP('2024-01-01 00:00:00') * 1000, UNIX_TIMESTAMP('2025-12-31 23:59:59') * 1000, 30, 1, 'Giảm 50.000đ cho đơn hàng từ 200.000đ'),
('voucher_004', 'NEWYEAR2025', 15.00, 0.00, 'PERCENT', 0.00, NULL, UNIX_TIMESTAMP('2025-01-01 00:00:00') * 1000, UNIX_TIMESTAMP('2025-01-31 23:59:59') * 1000, 200, 0, 'Mã giảm giá năm mới - Giảm 15%'),
('voucher_005', 'WEEKEND', 0.00, 30000.00, 'AMOUNT', 150000.00, 30000.00, UNIX_TIMESTAMP('2024-01-01 00:00:00') * 1000, UNIX_TIMESTAMP('2025-12-31 23:59:59') * 1000, 0, 0, 1, 'Giảm 30.000đ cho đơn hàng cuối tuần từ 150.000đ');

-- Insert Orders (Sample Orders)
INSERT INTO `orders` (`order_id`, `user_id`, `total_price`, `order_date`, `status`, `delivery_address`, `phone_number`, `customer_name`, `payment_method`) VALUES
('order_001', 'user_001', 150000.00, UNIX_TIMESTAMP('2024-12-01 10:30:00') * 1000, 'COMPLETED', '123 Đường ABC, Quận 1, TP.HCM', '0987654321', 'Nguyễn Văn A', 'CASH'),
('order_002', 'user_002', 250000.00, UNIX_TIMESTAMP('2024-12-05 14:20:00') * 1000, 'PENDING', '456 Đường XYZ, Quận 2, TP.HCM', '0912345678', 'Trần Thị B', 'CARD'),
('order_003', 'user_001', 180000.00, UNIX_TIMESTAMP('2024-12-10 09:15:00') * 1000, 'PROCESSING', '123 Đường ABC, Quận 1, TP.HCM', '0987654321', 'Nguyễn Văn A', 'CASH'),
('order_004', 'user_003', 320000.00, UNIX_TIMESTAMP('2024-12-15 16:45:00') * 1000, 'COMPLETED', '789 Đường DEF, Quận 3, TP.HCM', '0923456789', 'Lê Văn C', 'CARD'),
('order_005', 'user_002', 95000.00, UNIX_TIMESTAMP('2024-12-20 11:00:00') * 1000, 'CANCELLED', '456 Đường XYZ, Quận 2, TP.HCM', '0912345678', 'Trần Thị B', 'CASH');

-- Insert Order Items
INSERT INTO `order_items` (`order_id`, `item_title`, `item_json`, `quantity`, `price`, `subtotal`) VALUES
('order_001', 'Mocha Espresso', '{"id":1,"name":"Mocha Espresso","price":3.80}', 2, 3.80, 76000.00),
('order_001', 'Classic Latte Foam', '{"id":17,"name":"Classic Latte Foam","price":4.50}', 1, 4.50, 45000.00),
('order_002', 'Matcha Latte Leaf Art', '{"id":15,"name":"Matcha Latte Leaf Art","price":4.90}', 3, 4.90, 147000.00),
('order_002', 'Brown Sugar Latte', '{"id":24,"name":"Brown Sugar Latte","price":5.40}', 2, 5.40, 108000.00),
('order_003', 'Italian Cappuccino', '{"id":9,"name":"Italian Cappuccino","price":4.50}', 2, 4.50, 90000.00),
('order_003', 'Classic Americano', '{"id":28,"name":"Classic Americano","price":3.20}', 2, 3.20, 64000.00),
('order_004', 'Strawberry Cheese Latte', '{"id":27,"name":"Strawberry Cheese Latte","price":6.00}', 3, 6.00, 180000.00),
('order_004', 'Matcha Bear Cream Latte', '{"id":23,"name":"Matcha Bear Cream Latte","price":6.20}', 2, 6.20, 124000.00),
('order_005', 'Light Japanese Espresso', '{"id":3,"name":"Light Japanese Espresso","price":3.50}', 1, 3.50, 35000.00),
('order_005', 'Iced Americano', '{"id":29,"name":"Iced Americano","price":3.40}', 1, 3.40, 34000.00);

-- Insert Banners
INSERT INTO `banners` (`title`, `description`, `imageUrl`, `linkUrl`, `displayOrder`, `isActive`) VALUES
('Khuyến mãi đặc biệt', 'Giảm giá lên đến 30% cho tất cả sản phẩm', 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/banner1.jpg', '/products', 1, 1),
('Sản phẩm mới', 'Khám phá các loại cà phê mới nhất', 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/banner2.jpg', '/products?new=true', 2, 1),
('Combo tiết kiệm', 'Mua combo và tiết kiệm hơn 20%', 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/banner3.jpg', '/products?combo=true', 3, 1);

-- =====================================================
-- PHẦN 4: CẬP NHẬT AUTO_INCREMENT
-- =====================================================

ALTER TABLE `categories` AUTO_INCREMENT = 6;
ALTER TABLE `products` AUTO_INCREMENT = 36;
ALTER TABLE `order_items` AUTO_INCREMENT = 11;
ALTER TABLE `banners` AUTO_INCREMENT = 4;

-- =====================================================
-- PHẦN 5: KIỂM TRA DỮ LIỆU
-- =====================================================

-- Kiểm tra số lượng records
SELECT 'Categories' AS TableName, COUNT(*) AS RecordCount FROM `categories`
UNION ALL
SELECT 'Products', COUNT(*) FROM `products`
UNION ALL
SELECT 'Users', COUNT(*) FROM `users`
UNION ALL
SELECT 'Vouchers', COUNT(*) FROM `vouchers`
UNION ALL
SELECT 'Orders', COUNT(*) FROM `orders`
UNION ALL
SELECT 'Order Items', COUNT(*) FROM `order_items`
UNION ALL
SELECT 'Banners', COUNT(*) FROM `banners`;

-- =====================================================
-- HOÀN TẤT
-- =====================================================
-- Database đã được tạo hoàn chỉnh với tất cả các bảng và dữ liệu mẫu
-- Bạn có thể sử dụng database này để phát triển và test ứng dụng
-- =====================================================

