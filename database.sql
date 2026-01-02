
DROP DATABASE IF EXISTS greengrocer_db;
CREATE DATABASE greengrocer_db;
USE greengrocer_db;

CREATE TABLE UserInfo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(64) NOT NULL,
    role VARCHAR(20) NOT NULL,
    address VARCHAR(255),
    loyalty_points INT DEFAULT 0
);

CREATE TABLE Coupons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) UNIQUE,
    discount_amount DOUBLE,
    min_spend DOUBLE,
    active BOOLEAN DEFAULT TRUE,
    point_cost INT DEFAULT 0,
    description TEXT
);

CREATE TABLE UserCoupons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    coupon_id INT,
    used BOOLEAN DEFAULT FALSE,
    acquired_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES UserInfo(id),
    FOREIGN KEY (coupon_id) REFERENCES Coupons(id)
);

CREATE TABLE ProductInfo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(20) NOT NULL,
    price DOUBLE NOT NULL,
    stock DOUBLE NOT NULL,
    threshold DOUBLE NOT NULL,
    imagelocation BLOB
);

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

CREATE TABLE Messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT,
    receiver_id INT,
    content TEXT,
    reply TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES UserInfo(id)
);


INSERT INTO UserInfo (username, password, role, address) VALUES 
('cust', '80d26609c5226268981e4a6d4ceddbc339d991841ae580e3180b56c8ade7651d', 'customer', '123 Apple St'),
('carr', 'f9356b0952e5681f9bb4969078d6762f1f3f3eb9e87b80d6544103ad918f074c', 'carrier', 'Carrier Station'),
('own', '5b3975651c3cab92d044c096dc30a1c2d9525497457472de48c51ecb363d1f4a', 'owner', 'HQ');

INSERT INTO Coupons (code, discount_amount, min_spend, point_cost, description) VALUES
('WELCOME2025', 10.0, 50.0, 0, 'Welcome coupon'),
('SAVE5', 5.0, 25.0, 50, 'Small discount for starters'),
('SAVE10', 10.0, 50.0, 100, 'Good value discount'),
('SAVE20', 20.0, 100.0, 200, 'Great savings!'),
('SAVE50', 50.0, 200.0, 500, 'Premium discount');


SET @path = '/Users/nucako/Documents/GitHub/OOP_Project_3/src/main/resources/images/';


TRUNCATE TABLE ProductInfo;


INSERT INTO ProductInfo (name, type, price, stock, threshold, imagelocation) VALUES 
('Tomato', 'Vegetable', 3.50, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'tomato.png'))),
('Potato', 'Vegetable', 2.00, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'potato.png'))),
('Onion', 'Vegetable', 2.50, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'onion.png'))),
('Carrot', 'Vegetable', 3.00, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'carrot.png'))),
('Cucumber', 'Vegetable', 2.80, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'cucumber.png'))),
('Pepper', 'Vegetable', 4.00, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'pepper.png'))),
('Lettuce', 'Vegetable', 1.50, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'lettuce.png'))),
('Spinach', 'Vegetable', 3.20, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'spinach.png'))),
('Broccoli', 'Vegetable', 4.50, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'broccoli.png'))),
('Cauliflower', 'Vegetable', 4.00, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'cauliflower.png'))),
('Garlic', 'Vegetable', 8.00, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'garlic.png'))),
('Zucchini', 'Vegetable', 3.00, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'zucchini.png')));


INSERT INTO ProductInfo (name, type, price, stock, threshold, imagelocation) VALUES 
('Apple', 'Fruit', 5.00, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'apple.png'))),
('Banana', 'Fruit', 6.00, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'banana.png'))),
('Orange', 'Fruit', 4.50, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'orange.png'))),
('Grape', 'Fruit', 7.00, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'grape.png'))),
('Strawberry', 'Fruit', 8.00, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'strawberry.png'))),
('Watermelon', 'Fruit', 1.00, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'watermelon.png'))),
('Melon', 'Fruit', 1.50, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'melon.png'))),
('Peach', 'Fruit', 5.50, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'peach.png'))),
('Pear', 'Fruit', 5.00, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'pear.png'))),
('Cherry', 'Fruit', 10.00, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'cherry.png'))),
('Plum', 'Fruit', 6.50, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'plum.png'))),
('Kiwi', 'Fruit', 7.50, 100.0, 10.0, LOAD_FILE(CONCAT(@path, 'kiwi.png')));
