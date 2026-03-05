package com.novapay.payments;

import com.novapay.payments.idempotency.IdempotencyHandler;
import com.novapay.payments.model.PaymentRequest;
import com.novapay.payments.model.PaymentResult;

public class PaymentService {

    private final TransactionEngine transactionEngine;
    private final IdempotencyHandler idempotencyHandler;
    private final WebhookService webhookService;

    public PaymentService(TransactionEngine engine,
                          IdempotencyHandler idempotency,
                          WebhookService webhooks) {
        this.transactionEngine = engine;
        this.idempotencyHandler = idempotency;
        this.webhookService = webhooks;
    }

    public PaymentResult processPayment(PaymentRequest request) {
        PaymentResult cached = idempotencyHandler.get(request.getIdempotencyKey());
        if (cached != null) {
            return cached;
        }

        PaymentResult result = transactionEngine.execute(request);
        idempotencyHandler.store(request.getIdempotencyKey(), result);
        webhookService.emit("payment.completed", result);
        return result;
    }
}
