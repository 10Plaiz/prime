-- Insert sample Customers
INSERT OR IGNORE INTO Customer (CustomerID, Name, Address, Contact, Username, Pin) VALUES 
(1, 'John Doe', '123 Main St, Anytown', 'john.doe@example.com', 'johndoe', '1234'),
(2, 'Jane Smith', '456 Oak Ave, Somewhere', 'jane.smith@example.com', 'janesmith', '5678');

-- Insert Sample Accounts (Linking to the 2 customers created above)
INSERT OR IGNORE INTO Account (AccountID, CustomerID, Type, Balance) VALUES 
(1, 1, 'Savings', 1500.00),
(2, 2, 'Checking', 3500.50);

-- Insert Sample Transactions
INSERT OR IGNORE INTO Transactions (TransactionID, AccountID, Type, Amount) VALUES 
(1, 1, 'deposit', 2000.00),
(2, 1, 'withdrawal', 500.00),
(3, 2, 'deposit', 3500.50);

-- Insert Sample Beneficiaries
INSERT OR IGNORE INTO Beneficiary (BeneficiaryID, CustomerID, Name, AccountNumber, BankDetails) VALUES 
(1, 1, 'Alice Johnson', 'ACCT123456789', 'Bank of America'),
(2, 2, 'Bob Williams', 'ACCT987654321', 'Chase Bank');
