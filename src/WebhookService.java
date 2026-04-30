package com.novapay.payments;

import com.novapay.payments.model.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebhookService {
    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private static final int MAX_RETRIES = 3;

    public void emit(String eventType, PaymentResult payload) {
        int attempts = 0;
        boolean success = false;

        while (attempts < MAX_RETRIES && !success) {
            try {
                attempts++;
                // Simulate dispatch to registered merchant webhook endpoints
                dispatchWebhook(eventType, payload);
                success = true;
                log.info("Webhook emitted successfully for event: {}", eventType);
            } catch (Exception e) {
                log.error("Failed to emit webhook on attempt {} for event {}: {}", attempts, eventType, e.getMessage());
                if (attempts == MAX_RETRIES) {
                    log.error("Max retries reached for webhook event: {}. Giving up.", eventType);
                }
                // Optionally, add delay or exponential backoff here
            }
        }
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
