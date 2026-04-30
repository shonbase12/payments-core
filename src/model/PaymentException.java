package com.novapay.payments.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentException extends RuntimeException {
    private static final Logger logger = LoggerFactory.getLogger(PaymentException.class);
    private final String errorCode;

    public PaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        logger.error("PaymentException created with errorCode: {} and message: {}", errorCode, message);
    }

    public String getErrorCode() { return errorCode; }
}
