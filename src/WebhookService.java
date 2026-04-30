package com.novapay.payments;

import com.novapay.payments.model.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebhookService {
    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private int maxRetries = 3;
    private long baseDelayMillis = 500;

    public WebhookService() {}

    public WebhookService(int maxRetries, long baseDelayMillis) {
        this.maxRetries = maxRetries;
        this.baseDelayMillis = baseDelayMillis;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setBaseDelayMillis(long baseDelayMillis) {
        this.baseDelayMillis = baseDelayMillis;
    }

    public void emit(String eventType, PaymentResult payload) {
        int attempts = 0;
        boolean success = false;

        while (attempts < maxRetries && !success) {
            try {
                attempts++;
                dispatchWebhook(eventType, payload);
                success = true;
                log.info("Webhook emitted successfully for event: {}", eventType);
            } catch (Exception e) {
                log.error("Failed to emit webhook on attempt {} for event {}", attempts, eventType, e);
                if (attempts == maxRetries) {
                    log.error("Max retries reached for webhook event: {}. Giving up.", eventType);
                } else {
                    // Exponential backoff with jitter
                    long delay = (long) (baseDelayMillis * Math.pow(2, attempts - 1));
                    long jitter = (long) (Math.random() * baseDelayMillis);
                    long sleepTime = delay + jitter;
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("Retry sleep interrupted", ie);
                        break;
                    }
                }
            }
        }
    }

    protected void dispatchWebhook(String eventType, PaymentResult payload) throws Exception {
        // Actual HTTP dispatch logic to merchant webhook URLs goes here
        // For now, simulate success or failure randomly
        if (Math.random() < 0.7) {
            return;
        } else {
            throw new Exception("Simulated webhook dispatch failure");
        }
    }
}
