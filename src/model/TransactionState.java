package com.novapay.payments.model;

public enum TransactionState {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REVERSED,
    ON_HOLD,
    EXPIRED,
    FAILED_VALIDATION;
}