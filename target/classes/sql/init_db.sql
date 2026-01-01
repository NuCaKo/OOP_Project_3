-- Initialize Database
CREATE DATABASE IF NOT EXISTS greengrocer_db;
USE greengrocer_db;

-- UserInfo Table
CREATE TABLE IF NOT EXISTS UserInfo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL,
    address VARCHAR(255),
    loyalty_points INT DEFAULT 0
);

-- Coupons Table
CREATE TABLE IF NOT EXISTS Coupons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) UNIQUE,
    discount_amount DOUBLE,
    min_spend DOUBLE,
    active BOOLEAN DEFAULT TRUE
);

-- ProductInfo Table
CREATE TABLE IF NOT EXISTS ProductInfo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    price DOUBLE NOT NULL,
    stock DOUBLE NOT NULL,
    threshold DOUBLE NOT NULL,
    imagelocation BLOB
);

-- OrderInfo Table
CREATE TABLE IF NOT EXISTS OrderInfo (
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

-- Messages Table
CREATE TABLE IF NOT EXISTS Messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT,
    receiver_id INT,
    content TEXT,
    reply TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES UserInfo(id)
);

-- Seed Data (Users)
INSERT IGNORE INTO UserInfo (username, password, role, address) VALUES 
('cust', 'cust', 'customer', '123 Apple St'),
('carr', 'carr', 'carrier', 'Carrier Station'),
('own', 'own', 'owner', 'HQ');

-- Seed Data (Coupons)
INSERT IGNORE INTO Coupons (code, discount_amount, min_spend) VALUES 
('WELCOME2025', 10.0, 50.0);
