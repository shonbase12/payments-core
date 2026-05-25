package com.novapay.payments;

import com.novapay.payments.model.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class WebhookService {
    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_BACKOFF_MS = 100;
    private static final double BACKOFF_MULTIPLIER = 2.0;
    private static final long MAX_BACKOFF_MS = 1000;
    private static final Random random = new Random();

    public void emit(String eventType, PaymentResult payload) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                dispatchWebhook(eventType, payload);
                log.info("Webhook emitted successfully for event: {}", eventType);
                return;
            } catch (Exception e) {
                log.error("Failed to emit webhook on attempt {} for event {}: {}", attempt, eventType, e.getMessage(), e);
                if (attempt == MAX_RETRIES) {
                    log.error("Max retries reached for webhook event: {}. Giving up.", eventType);
                } else {
                    long backoffTime = calculateBackoffTime(attempt);
                    log.info("Retrying after {} ms", backoffTime);
                    try {
                        Thread.sleep(backoffTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Retry sleep interrupted", ie);
                        break;
                    }
                }
            }
        }
    }

    private long calculateBackoffTime(int attempt) {
        long exponentialBackoff = (long) (INITIAL_BACKOFF_MS * Math.pow(BACKOFF_MULTIPLIER, attempt - 1));
        long jitter = (long) (random.nextDouble() * INITIAL_BACKOFF_MS);
        return Math.min(exponentialBackoff + jitter, MAX_BACKOFF_MS);
    }

    private void dispatchWebhook(String eventType, PaymentResult payload) throws Exception {
        // TODO: Implement actual HTTP dispatch logic to merchant webhook URLs
        // For now, simulate success or failure randomly
        if (Math.random() < 0.7) {
            // Simulate success
            return;
        } else {
            throw new Exception("Simulated webhook dispatch failure");
        }
    }
}
