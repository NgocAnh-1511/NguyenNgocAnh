-- Script để xóa dữ liệu cũ trong bảng categories và products
-- Chạy script này TRƯỚC khi chạy create_categories_products_from_firebase.sql
-- nếu gặp lỗi duplicate

USE `CoffeShop`;

-- Tắt foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa tất cả dữ liệu trong bảng
TRUNCATE TABLE `products`;
TRUNCATE TABLE `categories`;

-- Hoặc xóa bảng hoàn toàn
-- DROP TABLE IF EXISTS `products`;
-- DROP TABLE IF EXISTS `categories`;

-- Bật lại foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

