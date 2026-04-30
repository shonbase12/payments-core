package com.novapay.payments.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum TransactionState {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED;
    // Add additional states as necessary

    private static final Logger logger = LoggerFactory.getLogger(TransactionState.class);

    public void logTransition(TransactionState fromState, String transactionId) {
        logger.info("Transaction {} state changed from {} to {}", transactionId, fromState, this);
    }
}
