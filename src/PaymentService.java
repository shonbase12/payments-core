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
        // Validate the payment request
        if (!isValidPaymentRequest(request)) {
            log.error("Invalid payment request for ID: " + (request != null ? request.getIdempotencyKey() : "null"));
            throw new InvalidPaymentRequestException("Invalid payment request");
        }

        log.info("Processing payment for request ID: " + request.getIdempotencyKey());
        PaymentResult cached = idempotencyHandler.get(request.getIdempotencyKey());
        if (cached != null) {
            log.info("Returning cached result for request ID: " + request.getIdempotencyKey());
            return cached;
        }

        PaymentResult result;
        try {
            result = transactionEngine.execute(request);
            idempotencyHandler.store(request.getIdempotencyKey(), result);
            // Emit webhook asynchronously
            CompletableFuture.runAsync(() -> webhookService.emit("payment.completed", result));
            return result;
        } catch (TransactionException e) {
            log.error("Transaction failed: " + e.getMessage());
            throw new PaymentProcessingException("Transaction failed", e);
        } catch (Exception e) {
            log.error("An error occurred while processing payment: " + e.getMessage());
            throw new PaymentProcessingException("An unexpected error occurred", e);
        }
    }

    private boolean isValidPaymentRequest(PaymentRequest request) {
        if (request == null) {
            log.error("PaymentRequest is null");
            return false;
        }
        if (request.getIdempotencyKey() == null) {
            log.error("IdempotencyKey is missing in the PaymentRequest");
            return false;
        }
        if (request.getAmount() <= 0) {
            log.error("Invalid payment amount: " + request.getAmount());
            return false;
        }
        // Additional validations can be added here
        return true;
    }
}
