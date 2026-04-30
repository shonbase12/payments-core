package com.novapay.payments;

import com.novapay.payments.idempotency.IdempotencyHandler;
import com.novapay.payments.model.PaymentRequest;
import com.novapay.payments.model.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;

public class PaymentService {

    private final TransactionEngine transactionEngine;
    private final IdempotencyHandler idempotencyHandler;
    private final WebhookService webhookService;
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    public PaymentService(TransactionEngine engine,
                          IdempotencyHandler idempotency,
                          WebhookService webhooks) {
        this.transactionEngine = engine;
        this.idempotencyHandler = idempotency;
        this.webhookService = webhooks;
    }

    public PaymentResult processPayment(PaymentRequest request) {
        if (!isValidPaymentRequest(request)) {
            log.error("Invalid payment request for ID: {}", request != null ? request.getIdempotencyKey() : "null");
            throw new InvalidPaymentRequestException("Invalid payment request");
        }

        log.info("Processing payment for request ID: {}", request.getIdempotencyKey());
        PaymentResult cached = idempotencyHandler.get(request.getIdempotencyKey());
        if (cached != null) {
            log.info("Returning cached result for request ID: {}", request.getIdempotencyKey());
            return cached;
        }

        PaymentResult result;
        try {
            result = transactionEngine.execute(request);
            idempotencyHandler.store(request.getIdempotencyKey(), result);
            CompletableFuture.runAsync(() -> webhookService.emit("payment.completed", result));
            return result;
        } catch (TransactionException e) {
            log.error("Transaction failed for request ID: {}. Error: {}", request.getIdempotencyKey(), e.getMessage(), e);
            throw new PaymentProcessingException("Transaction failed", e);
        } catch (Exception e) {
            log.error("Unexpected error occurred while processing payment for request ID: {}. Error: {}", request.getIdempotencyKey(), e.getMessage(), e);
            throw new PaymentProcessingException("An unexpected error occurred", e);
        }
    }

    private boolean isValidPaymentRequest(PaymentRequest request) {
        if (request == null) {
            log.error("PaymentRequest is null");
            return false;
        }
        if (request.getIdempotencyKey() == null || !request.getIdempotencyKey().matches("^[a-zA-Z0-9]{10,}$")) {
            log.error("IdempotencyKey is missing or invalid in the PaymentRequest");
            return false;
        }
        if (request.getAmount() <= 0 || request.getAmount() > 10000) {
            log.error("Invalid payment amount: {}", request.getAmount());
            return false;
        }
        return true;
    }
}
