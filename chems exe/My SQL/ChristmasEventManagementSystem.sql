-- Create the database
CREATE DATABASE christmaseventmanagementsystem;

-- Use the database
USE christmaseventmanagementsystem;

-- Create tables
-- 01. Creating a ChemsUser Table
CREATE TABLE chemsuser (
    chems_id            VARCHAR(5)		NOT NULL UNIQUE CHECK (chems_id LIKE 'CH%'),
    user_type			VARCHAR(7)		NOT NULL,
    nibm_id             VARCHAR(15)		PRIMARY KEY,
    encryptedNibm_id	VARCHAR(35),
    member_name         VARCHAR(40)		NOT NULL,
    batch_number		VARCHAR(15)		NOT NULL,
    email 				VARCHAR(255) 	NOT NULL CHECK (email LIKE '%@student.nibm.lk' OR email LIKE '%@nibm.lk' OR email LIKE '%@vl.nibm.lk' OR email LIKE '%@gmail.com'),
    payment             VARCHAR(7)		NOT NULL CHECK (payment IN ('Paid', 'Pending', 'Free')),
    discount			VARCHAR(5),
    price				VARCHAR(5),
	registerDT			DATETIME 		NOT NULL DEFAULT CURRENT_TIMESTAMP,
    isUpdateMember		VARCHAR(3)		CHECK (isUpdateMember IN ('Yes', 'No')),
    updatedDT			DATETIME
);

-- 02. Creating a ScanChemsUser Table
CREATE TABLE scanchemsuser (
  chems_id				varchar(5)		NOT NULL CHECK (chems_id like 'CH%'),
  nibm_id				varchar(15) 	NOT NULL,
  encryptedNibm_id		varchar(35) 	NOT NULL,
  member_name			varchar(40) 	NOT NULL,
  email					varchar(255) 	NOT NULL CHECK (email LIKE '%@student.nibm.lk' OR email LIKE '%@nibm.lk' OR email LIKE '%@vl.nibm.lk' OR email LIKE '%@gmail.com'),
  price					varchar(5) 		DEFAULT NULL,
  scanDT				datetime 		DEFAULT NULL,
  isCompletedEntering	char(3) 		DEFAULT NULL CHECK (isCompletedEntering in ('Yes','No')),
  entryDT				datetime 		DEFAULT NULL,
  isFinishedLunch		char(3) 		DEFAULT NULL CHECK (isFinishedLunch in ('Yes','No')),
  lunchDT				datetime 		DEFAULT NULL,
  FOREIGN KEY (chems_id) REFERENCES chemsuser (chems_id),
  FOREIGN KEY (nibm_id) REFERENCES chemsuser (nibm_id)
);

-- Create permission
-- 01. Create a User
CREATE USER 'admin'@'localhost' IDENTIFIED BY 'admin@123';
-- 02. Grant Permissions on a specific table to the user
GRANT INSERT, UPDATE, DELETE, SELECT ON christmaseventmanagementsystem.chemsUser TO 'admin'@'localhost';
-- 03. Apply the changes
FLUSH PRIVILEGES;

SHOW GRANTS FOR 'admin'@'localhost';