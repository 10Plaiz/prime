# Bank System

This Java-based banking system project built with Maven and Swing. It is designed as a simple ATM/banking prototype that supports customer accounts, transaction history, and beneficiary management.

## Features

- ATM-style login screen
- Customer and account data model
- Account balance tracking
- Deposit and withdrawal transaction records
- Beneficiary records for customers
- Sample database seed data for testing
- Maven-based build and run setup

## Project Structure

```text
prime/
├── bank_system/
│   ├── pom.xml
│   └── src/
│       └── main/
│           └── java/
│               ├── Login.java
│               └── com/mycompany/bank_system/Bank_system.java
├── database.sql
├── seed_data.sql
├── build.sh
└── README.md
```

## Tech Stack
- Java
- Swing for the desktop UI
- Maven for project management and build
- SQLite / SQL for data storage and schema setup

## Database Tables

Database Tables he database includes the following tables:

1. Customer — stores user account details
2. Account — stores account type and balance
3. Transactions — stores deposits and withdrawals
4. Beneficiary — stores saved beneficiaries for customers

## Sample Data
The repository includes seed data for:

- sample customers
- sample accounts
- sample transactions
- sample beneficiaries

## How to Run

### Prerequisites
1. Java installed
2. Maven installed

### Build and run
cd bank_system
mvn clean compile exec:java

or 

./build.sh
