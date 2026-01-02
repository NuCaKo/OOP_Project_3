-- Database Setup Script for Local Greengrocer App
-- Run this script to reset and initialize the database manually.

DROP DATABASE IF EXISTS greengrocer_db;
CREATE DATABASE greengrocer_db;
USE greengrocer_db;

-- 1. UserInfo Table
CREATE TABLE UserInfo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    address VARCHAR(255),
    loyalty_points INT DEFAULT 0
);

-- 2. Coupons Table
CREATE TABLE Coupons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) UNIQUE,
    discount_amount DOUBLE,
    min_spend DOUBLE,
    active BOOLEAN DEFAULT TRUE
);

-- 3. ProductInfo Table
CREATE TABLE ProductInfo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    price DOUBLE NOT NULL,
    stock DOUBLE NOT NULL,
    threshold DOUBLE NOT NULL,
    imagelocation BLOB
);

-- 4. OrderInfo Table
CREATE TABLE OrderInfo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ordertime DATETIME,
    deliverytime DATETIME,
    products TEXT,
    user_id INT,
    carrier_id INT,
    isdelivered BOOLEAN DEFAULT FALSE,
    totalcost DOUBLE,
    invoice MEDIUMTEXT,
    carrier_rating INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES UserInfo(id)
);

-- 5. Messages Table
CREATE TABLE Messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT,
    receiver_id INT,
    content TEXT,
    reply TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES UserInfo(id)
);

-- 6. UserCoupons Table (Customer's owned coupons)
CREATE TABLE IF NOT EXISTS UserCoupons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    coupon_id INT,
    acquired_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    used BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES UserInfo(id),
    FOREIGN KEY (coupon_id) REFERENCES Coupons(id)
);

-- ==========================================
-- SEED DATA
-- ==========================================

-- Users
INSERT INTO UserInfo (username, password, role, address) VALUES 
('cust', 'cust', 'customer', '123 Apple St'),
('carr', 'carr', 'carrier', 'Carrier Station'),
('own', 'own', 'owner', 'HQ');

-- Coupons
INSERT INTO Coupons (code, discount_amount, min_spend) VALUES 
('WELCOME2025', 10.0, 50.0);

-- Products (Vegetables) - Images are initialized as NULL here. 
-- The Java application will generate/seed images upon first run if they are missing.
INSERT INTO ProductInfo (name, type, price, stock, threshold) VALUES 
('Tomato', 'Vegetable', 3.50, 100.0, 10.0),
('Potato', 'Vegetable', 2.00, 100.0, 10.0),
('Onion', 'Vegetable', 2.50, 100.0, 10.0),
('Carrot', 'Vegetable', 3.00, 100.0, 10.0),
('Cucumber', 'Vegetable', 2.80, 100.0, 10.0),
('Pepper', 'Vegetable', 4.00, 100.0, 10.0),
('Lettuce', 'Vegetable', 1.50, 100.0, 10.0),
('Spinach', 'Vegetable', 3.20, 100.0, 10.0),
('Broccoli', 'Vegetable', 4.50, 100.0, 10.0),
('Cauliflower', 'Vegetable', 4.00, 100.0, 10.0),
('Garlic', 'Vegetable', 8.00, 100.0, 10.0),
('Zucchini', 'Vegetable', 3.00, 100.0, 10.0);

-- Products (Fruits)
INSERT INTO ProductInfo (name, type, price, stock, threshold) VALUES 
('Apple', 'Fruit', 5.00, 100.0, 10.0),
('Banana', 'Fruit', 6.00, 100.0, 10.0),
('Orange', 'Fruit', 4.50, 100.0, 10.0),
('Grape', 'Fruit', 7.00, 100.0, 10.0),
('Strawberry', 'Fruit', 8.00, 100.0, 10.0),
('Watermelon', 'Fruit', 1.00, 100.0, 10.0),
('Melon', 'Fruit', 1.50, 100.0, 10.0),
('Peach', 'Fruit', 5.50, 100.0, 10.0),
('Pear', 'Fruit', 5.00, 100.0, 10.0),
('Cherry', 'Fruit', 10.00, 100.0, 10.0),
('Plum', 'Fruit', 6.50, 100.0, 10.0),
('Kiwi', 'Fruit', 7.50, 100.0, 10.0);
