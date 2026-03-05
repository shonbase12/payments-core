package com.novapay.payments;

import com.novapay.payments.model.PaymentResult;

public class WebhookService {
    public void emit(String eventType, PaymentResult payload) {
        // Dispatch to registered merchant webhook endpoints
        // Retries handled by the retry queue
    }
}
