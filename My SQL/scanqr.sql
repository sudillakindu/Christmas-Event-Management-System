-- Create the database
CREATE DATABASE christmaseventmanagementsystem;

USE christmaseventmanagementsystem;

-- Create tables
-- 01. Creating a ChemsUser Table
CREATE TABLE chemsuser (
    chems_id            VARCHAR(5)		NOT NULL UNIQUE CHECK (chems_id LIKE 'CH%'),
    user_type			VARCHAR(7)		NOT NULL,
    nibm_id             VARCHAR(15)		PRIMARY KEY,
    encryptedNibm_id	VARCHAR(35),
    member_name         VARCHAR(40)		NOT NULL,
    batch_number		VARCHAR(15),
    email 				VARCHAR(255) 	NOT NULL CHECK (email LIKE '%@student.nibm.lk' OR email LIKE '%@gmail.com'),
    payment             VARCHAR(7)		NOT NULL CHECK (payment IN ('Paid', 'Pending', 'Free')),
    discount			VARCHAR(5),
    price				VARCHAR(5),
	registerDT			DATETIME 		NOT NULL DEFAULT CURRENT_TIMESTAMP,
    isUpdateMember		VARCHAR(3)		CHECK (isUpdateMember IN ('Yes', 'No')),
    updatedDT			DATETIME
);

-- 01. Grant Permissions on a specific table to the user
GRANT INSERT, UPDATE, DELETE, SELECT ON christmaseventmanagementsystem.chemsUser TO 'admin'@'localhost';