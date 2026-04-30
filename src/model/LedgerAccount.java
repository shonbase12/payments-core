package com.novapay.payments.model;

import java.math.BigDecimal;
import java.util.UUID;

public class LedgerAccount {
    public enum AccountType {
        ASSET,
        LIABILITY,
        EQUITY,
        REVENUE,
        EXPENSE
    }

    private final String id;
    private final String name;
    private final AccountType type;
    private BigDecimal balance;

    public LedgerAccount(String name, AccountType type) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.balance = BigDecimal.ZERO;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AccountType getType() {
        return type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    // Debit operation increases asset and expense accounts, decreases liability, equity, and revenue
    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        switch (type) {
            case ASSET:
            case EXPENSE:
                balance = balance.add(amount);
                break;
            case LIABILITY:
            case EQUITY:
            case REVENUE:
                balance = balance.subtract(amount);
                break;
            default:
                throw new IllegalStateException("Unknown account type");
        }
    }

    // Credit operation increases liability, equity, and revenue accounts, decreases asset and expense
    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        switch (type) {
            case LIABILITY:
            case EQUITY:
            case REVENUE:
                balance = balance.add(amount);
                break;
            case ASSET:
            case EXPENSE:
                balance = balance.subtract(amount);
                break;
            default:
                throw new IllegalStateException("Unknown account type");
        }
    }

    @Override
    public String toString() {
        return "LedgerAccount{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", balance=" + balance +
                '}';
    }
}
