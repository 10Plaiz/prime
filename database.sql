-- Create Customer table 
CREATE TABLE IF NOT EXISTS Customer (
    CustomerID INTEGER PRIMARY KEY AUTOINCREMENT, 
    Name VARCHAR(255) NOT NULL,     
    Address VARCHAR(255),     
    Contact VARCHAR(255),     
    Username VARCHAR(50) UNIQUE NOT NULL,     
    Pin VARCHAR(255) NOT NULL
);

-- Create Account table 
CREATE TABLE IF NOT EXISTS Account (     
    AccountID INTEGER PRIMARY KEY AUTOINCREMENT,     
    CustomerID INT NOT NULL,
    Type VARCHAR(50),
    Balance DECIMAL(15, 2) DEFAULT 0.00,
    CONSTRAINT fk_customer FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID) ON DELETE CASCADE
);

-- Create Transaction table
CREATE TABLE IF NOT EXISTS Transactions (     
    TransactionID INTEGER PRIMARY KEY AUTOINCREMENT,     
    AccountID INT NOT NULL,     
    Type VARCHAR(20) NOT NULL CHECK (Type IN ('deposit', 'withdrawal')),     
    Amount DECIMAL(15, 2) NOT NULL,     
    Timestamp TEXT DEFAULT CURRENT_TIMESTAMP,     
    CONSTRAINT fk_account FOREIGN KEY (AccountID) REFERENCES Account(AccountID) ON DELETE CASCADE
);  

-- Create Beneficiary table 
CREATE TABLE IF NOT EXISTS Beneficiary (     
    BeneficiaryID INTEGER PRIMARY KEY AUTOINCREMENT,     
    CustomerID INT NOT NULL,     
    Name VARCHAR(255) NOT NULL,     
    AccountNumber VARCHAR(50) NOT NULL,     
    BankDetails VARCHAR(255),     
    CONSTRAINT fk_customer_beneficiary FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID) ON DELETE CASCADE
);
