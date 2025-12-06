-- Tạo bảng categories và products từ dữ liệu Firebase
-- Chạy script này trong MySQL Workbench hoặc Railway MySQL

USE `CoffeShop`;

-- Tắt foreign key checks để có thể xóa bảng
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa bảng cũ nếu có (để tạo lại)
DROP TABLE IF EXISTS `products`;
DROP TABLE IF EXISTS `categories`;

-- Bật lại foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Bảng categories
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

-- Bảng products
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

-- Insert Categories từ Firebase (sử dụng ON DUPLICATE KEY UPDATE để tránh lỗi)
INSERT INTO `categories` (`id`, `name`, `description`, `imageUrl`, `isActive`) VALUES
(0, 'Esspersso', NULL, NULL, 1),
(1, 'Cappuccino', NULL, NULL, 1),
(2, 'Latte', NULL, NULL, 1),
(3, 'Americano', NULL, NULL, 1),
(4, 'Hot Chocolate', NULL, NULL, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `description` = VALUES(`description`),
  `imageUrl` = VALUES(`imageUrl`),
  `isActive` = VALUES(`isActive`);

-- Tắt foreign key checks tạm thời để insert products
SET FOREIGN_KEY_CHECKS = 0;

-- Insert Products từ Firebase
INSERT INTO `products` (`name`, `description`, `price`, `originalPrice`, `imageUrl`, `stock`, `isActive`, `categoryId`) VALUES
-- Category 0: Esspersso
('Mocha Espresso', 'Rich espresso blended with chocolate for a bold, sweet flavor.', 3.80, 3.80, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006330/Espresso-_S%E1%BB%AFa_%C4%91en_v%C3%A0_soclola_cafe_dpjbzt.png', 0, 1, 0),
('Citrus Espresso', 'Espresso with a hint of citrus peel for a bright aftertaste.', 3.90, 3.90, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006330/Espresso-_cafe_v%C3%A0_v%E1%BB%8B_cam_raxdee.png', 0, 1, 0),
('Light Japanese Espresso', 'Smooth, clean espresso with a gentle roast profile.', 3.50, 3.50, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006331/Espresso-c%C3%A0_ph%C3%AA_xay_m%E1%BB%8Bn_v%C3%A0_n%C6%B0%E1%BB%9Bc_n%C3%B3ng_d%C6%B0%E1%BB%9Bi_%C3%A1p_su%E1%BA%A5t_cao_eukxgy.png', 0, 1, 0),
('Classic Espresso Shot', 'Traditional, full-bodied espresso with strong aroma.', 3.20, 3.20, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006363/Espresso-Cafe_nh%E1%BA%A1t_-_s%E1%BB%AFa_v%E1%BB%ABa_klidtm.png', 0, 1, 0),
('Ground Coffee Espresso', 'Deep-roasted espresso made from finely ground beans.', 3.40, 3.40, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006331/Espresso-c%C3%A0_ph%C3%AA_xay_m%E1%BB%8Bn_v%C3%A0_n%C6%B0%E1%BB%9Bc_n%C3%B3ng_d%C6%B0%E1%BB%9Bi_%C3%A1p_su%E1%BA%A5t_cao_eukxgy.png', 0, 1, 0),
('Dark Roast Espresso', 'Bold, intense espresso with caramelized notes.', 3.60, 3.60, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006326/Espresso_-_Cafe_h%E1%BA%A1t_v%C3%A0_%C4%90%C3%A1nh_b%E1%BB%8Dt_nhi%E1%BB%81u_k5mtfw.png', 0, 1, 0),
('Caramel Art Espresso', 'Espresso topped with light caramel heart art.', 3.80, 3.80, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006322/Espresso_-_Cafe_b%E1%BB%99t_v%C3%A0_l%E1%BB%9Bp_crema_b%E1%BB%8Dt_c%C3%A0_ph%C3%AA_v%C3%A0_h%C3%ACnh_tr%C3%A1i_tim_b%E1%BB%8Dt_aabgpm.png', 0, 1, 0),
('Espresso Fusion', 'Espresso fusion with creamy layers.', 4.50, 4.50, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/6_whjo3t.png', 0, 1, 0),

-- Category 2: Latte
('Matcha Latte Leaf Art', 'Premium matcha with steamed milk & latte art.', 4.90, 4.90, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034281/9_ra7zu2.png', 0, 1, 2),
('Classic Matcha Latte', 'Smooth matcha with a creamy and balanced profile.', 4.70, 4.70, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006352/Latte_-_Tr%C3%A0_xanh_v%C3%A0_s%E1%BB%AFa_%C4%91%E1%BA%ADm_b%C3%A9o_v%C3%A0_t%E1%BA%A1o_h%C3%ACnh_xeidvp.png', 0, 1, 2),
('Strong Matcha Latte', 'Deep green, bold matcha for tea lovers.', 4.80, 4.80, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006353/Latte_-_Tr%C3%A0_xanh_Vi%E1%BB%87t_Nam_v%C3%A0_b%E1%BB%99t_latte_s%E1%BB%AFa_v%C3%A0_s%E1%BB%AFa_m%E1%BB%8Fng_tr%C3%AAn_b%E1%BB%81_m%E1%BA%B7t_b2epim.png', 0, 1, 2),
('Vietnamese Matcha Latte', 'Traditional Vietnamese-style matcha with milk foam.', 4.60, 4.60, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006343/Latte_-_Tr%C3%A0_xanh_%C4%91%E1%BA%ADm_v%C3%A0_s%E1%BB%AFa_nh%E1%BA%A1t_sor0gj.png', 0, 1, 2),
('Matcha Latte with Pearls', 'Creamy matcha topped with chewy boba pearls.', 5.50, 5.50, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006353/Latte_-_Tr%C3%A0_xanh_Vi%E1%BB%87t_Nam_v%C3%A0_b%E1%BB%99t_latte_s%E1%BB%AFa_v%C3%A0_s%E1%BB%AFa_m%E1%BB%8Fng_tr%C3%A9n_b%E1%BB%81_m%E1%BA%B7t_b2epim.png', 0, 1, 2),
('3-Layer Matcha Latte', 'Matcha, milk & cream layered beautifully.', 5.20, 5.20, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006353/Latte_-_Tr%C3%A0_xanh_Vi%E1%BB%87t_Nam_v%C3%A0_b%E1%BB%99t_latte_s%E1%BB%AFa_v%C3%A0_s%E1%BB%AFa_m%E1%BB%8Fng_tr%C3%A9n_b%E1%BB%81_m%E1%BA%B7t_b2epim.png', 0, 1, 2),
('Classic Latte Foam', 'Smooth latte with thick milk foam.', 4.50, 4.50, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006339/Latte_-_B%E1%BB%99t_tr%C3%A0_xanh_nhi%E1%BB%85n_v%C3%A0_Ch%C3%A2n_Ch%C3%A2u_%C4%90en_kxe5l2.png', 0, 1, 2),
('Blueberry Cream Latte', 'Creamy latte topped with blueberry puree.', 5.80, 5.80, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006355/Latte_-_Tr%C3%A0nh_Xanh_Vi%E1%BB%87t_Qu%E1%BB%91c_v%C3%A0_kem_b%E1%BB%8Dt_soda_iloue7.png', 0, 1, 2),
('Matcha Bear Cream Latte', 'Matcha latte topped with bear whipped cream.', 6.20, 6.20, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006371/Latte_-_Latte_v%E1%BB%8B_truy%E1%BB%81n_th%E1%BB%91ng_v%C3%A0_kem_nhi%E1%BB%81u_nxz8h1.png', 0, 1, 2),
('Brown Sugar Latte', 'Milk & espresso topped with brown sugar drizzle.', 5.40, 5.40, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006341/Latte_-_S%E1%BB%AFa_kem_v%C3%A0_tr%C3%A0_xanh_nh%E1%BA%ADt_b%E1%BA%A3n_zmuobu.png', 0, 1, 2),
('Milk Cream Matcha Latte', 'Matcha latte topped with milk cream & syrup.', 5.60, 5.60, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006341/Latte_-_Latte_%C3%9D_v%E1%BB%8B_3_l%E1%BB%9Bp_f9vo3s.png', 0, 1, 2),
('Cherry Cream Latte', 'Cherry latte topped with whipped cream & syrup.', 5.90, 5.90, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006365/Latte_-_Latte_v%E1%BB%8B_d%C3%A2u_v%C3%A0_g%E1%BA%A5u_t%E1%BA%A1o_h%C3%ACnh_kem_ov9aob.png', 0, 1, 2),
('Strawberry Cheese Latte', 'Strawberry–cream latte with cheese foam.', 6.00, 6.00, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764080423/Latte_-_Latt_v%E1%BB%8B_cherry_v%C3%A0_%E1%BB%91ng_qu%E1%BA%BF_nhi%E1%BB%81u_kem_1_sv8xb3.png', 0, 1, 2),

-- Category 3: Americano
('Classic Americano', 'Black coffee brewed from premium roasted beans.', 3.20, 3.20, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006309/Americano_Tr%E1%BB%A9ng_-_C%C3%A0_ph%C3%AA_v%C3%A0_l%E1%BB%9Bp_tr%E1%BB%A9ng_m%E1%BB%8Fng_gbx30b.png', 0, 1, 3),
('Iced Americano', 'Refreshing iced americano with a bold finish.', 3.40, 3.40, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006309/Americano_Tr%E1%BA%AFng_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_t%C6%B0%C6%A1i_ux9yrk.png', 0, 1, 3),
('Egg Americano', 'Americano topped with fluffy egg cream.', 4.20, 4.20, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006307/Americano_Red_Eye_-_C%C3%A0_ph%C3%AA_n%C3%B3ng_%E1%BB%A7_l%C3%A2u_says57.png', 0, 1, 3),
('White Americano', 'Light iced americano with milk splash.', 3.70, 3.70, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006307/Americano_Tr%E1%BA%AFng_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_t%C6%B0%C6%A1i_ux9yrk.png', 0, 1, 3),
('Orange Americano', 'Citrus-infused iced americano with orange zest.', 4.00, 4.00, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006306/Americano_%C4%91%C3%A1_-_C%C3%A0_ph%C3%AA_v%C3%A0_n%C6%B0%E1%BB%9Bc_%C4%91%C3%A1_udyxfa.png', 0, 1, 3),
('Mint Americano', 'Iced americano with fresh mint.', 4.10, 4.10, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006309/Americano_B%E1%BA%A1c_H%C3%A0_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_pha_b%E1%BA%A1c_h%C3%A0_cu9etb.png', 0, 1, 3),
('Red Eye Americano', 'Americano with an extra espresso shot.', 4.50, 4.50, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006304/Americano_-_C%C3%A0_ph%C3%AA_v%C3%A0_n%C6%B0%E1%BB%9Bc_n%C3%B3ng_t2rlkf.png', 0, 1, 3),
('Cream Americano', 'Americano topped with silky whipped cream.', 4.30, 4.30, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006308/2Americano_kem_tr%E1%BB%A9ng_-_C%C3%A0_ph%C3%AA_v%C3%A0_tr%E1%BB%A9ng_1_x1lxrf.png', 0, 1, 3),

-- Category 1: Cappuccino
('Cappuccino Cocoa', 'Cappuccino topped with rich cocoa powder.', 4.30, 4.30, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006323/Cappuccino_%C3%9D_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_t%C6%B0%C6%A1i_%C4%91%C3%A1nh_b%E1%BB%8Dt_nhi%E1%BB%81u_truy%E1%BB%81n_th%E1%BB%91ng_yzn4gn.png', 0, 1, 1),
('Italian Cappuccino', 'Authentic Italian cappuccino topped with chocolate dust.', 4.50, 4.50, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006316/Cappuccino_Thi%C3%AAn_nga_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_t%C6%B0%C6%A1i_t%E1%BA%A1o_h%C3%ACnh_rnpfyo.png', 0, 1, 1),
('Animal Art Cappuccino', 'Cappuccino with cute animal latte art.', 4.80, 4.80, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006314/Cappuccino_t%E1%BA%A1o_h%C3%ACnh_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_t%C6%B0%C6%A1i_t%E1%BA%A1o_h%C3%ACnh_theo_%C3%BD_mu%E1%BB%91n_gqmn9x.png', 0, 1, 1),
('Heart Art Cappuccino', 'Cappuccino topped with heart latte art.', 4.70, 4.70, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006314/Cappuccino_t%E1%BA%A1o_h%C3%ACnh_-_C%C3%A0_ph%C3%AA_v%C3%A0_s%E1%BB%AFa_t%C6%B0%C6%A1i_t%E1%BA%A1o_h%C3%ACnh_theo_%C3%BD_mu%E1%BB%91n_gqmn9x.png', 0, 1, 1),
('Swan Art Cappuccino', 'Beautiful swan latte art on creamy cappuccino.', 5.00, 5.00, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1764006314/Cappuccino_ca_cao_-_C%C3%A0_ph%C3%AA_s%E1%BB%AFa_-_B%E1%BB%99t_ca_cao_sthuya.png', 0, 1, 1),
('Traditional Cappuccino', 'Balanced cappuccino with smooth foam.', 4.20, 4.20, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034282/10_l53vw4.png', 0, 1, 1),
('Leaf Art Cappuccino', 'Cappuccino with classic leaf latte art.', 4.40, 4.40, 'https://res.cloudinary.com/dpaovwqxs/image/upload/v1760034282/1_xrreub.png', 0, 1, 1);

-- Bật lại foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Cập nhật AUTO_INCREMENT để bắt đầu từ số tiếp theo
ALTER TABLE `categories` AUTO_INCREMENT = 5;
ALTER TABLE `products` AUTO_INCREMENT = 35;

