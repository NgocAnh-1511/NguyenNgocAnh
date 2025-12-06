-- Thêm category Espresso (Esspersso) vào database nếu chưa có
-- Chạy script này trong MySQL Workbench hoặc Railway MySQL

USE `CoffeShop`;

-- Kiểm tra và thêm category Espresso nếu chưa có
INSERT INTO `categories` (`id`, `name`, `description`, `imageUrl`, `isActive`, `createdAt`, `updatedAt`) 
VALUES (0, 'Esspersso', NULL, NULL, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `name` = 'Esspersso',
  `isActive` = 1,
  `updatedAt` = NOW();

-- Kiểm tra kết quả
SELECT * FROM `categories` WHERE `id` = 0 OR `name` LIKE '%Esspersso%' OR `name` LIKE '%Espresso%';

