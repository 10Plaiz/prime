package com.mycompany.bank_system;

public record CustomerSession(
        int customerId,
        int accountId,
        String name,
        String username,
        String accountType) {
}
