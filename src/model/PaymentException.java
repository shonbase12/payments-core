package com.novapay.payments.model;

import java.io.Serializable;

public class PaymentException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String errorCode;

    public PaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public PaymentException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "PaymentException{" +
                "errorCode='" + errorCode + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
