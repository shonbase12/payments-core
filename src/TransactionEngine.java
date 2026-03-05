package com.novapay.payments;

import com.novapay.payments.model.PaymentRequest;
import com.novapay.payments.model.PaymentResult;
import com.novapay.payments.model.TransactionState;

public class TransactionEngine {

    public PaymentResult execute(PaymentRequest request) {
        TransactionState state = TransactionState.PENDING;
        try {
            state = TransactionState.PROCESSING;
            String processorRef = callProcessor(request);
            state = TransactionState.COMPLETED;
            return PaymentResult.success(processorRef);
        } catch (PaymentException e) {
            state = TransactionState.FAILED;
            throw e;
        }
    }

    private String callProcessor(PaymentRequest request) {
        throw new UnsupportedOperationException("Processor not configured");
    }
}
