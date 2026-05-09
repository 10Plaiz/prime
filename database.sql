-- Create Customer table 
CREATE TABLE Customer (
    CustomerID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, 
    Name VARCHAR(255) NOT NULL,     
    Address VARCHAR(255),     
    Contact VARCHAR(255),     
    Username VARCHAR(50) UNIQUE NOT NULL,     
    Password VARCHAR(255) NOT NULL
);

-- Create Account table 
CREATE TABLE Account (     
    AccountID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,     
    CustomerID INT NOT NULL,
    Type VARCHAR(50),
    Balance DECIMAL(15, 2) DEFAULT 0.00,
    CONSTRAINT fk_customer FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID) ON DELETE CASCADE
);

-- Create Transaction table
CREATE TABLE Transactions (     
    TransactionID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,     
    AccountID INT NOT NULL,     
    Type VARCHAR(20) NOT NULL CHECK (Type IN ('deposit', 'withdrawal')),     
    Amount DECIMAL(15, 2) NOT NULL,     
    Timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,     
    CONSTRAINT fk_account FOREIGN KEY (AccountID) REFERENCES Account(AccountID) ON DELETE CASCADE
);  

-- Create Beneficiary table 
CREATE TABLE Beneficiary (     
    BeneficiaryID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,     
    CustomerID INT NOT NULL,     
    Name VARCHAR(255) NOT NULL,     
    AccountNumber VARCHAR(50) NOT NULL,     
    BankDetails VARCHAR(255),     
    CONSTRAINT fk_customer_beneficiary FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID) ON DELETE CASCADE
);