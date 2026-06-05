package com.novapay.payments;

import com.novapay.payments.model.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebhookService {
    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private static final int MAX_RETRIES = 3;

    public void emit(String eventType, PaymentResult payload) {
        int attempts = 0;
        boolean success = false;
        long backoffDelay = 1000; // initial backoff delay in milliseconds

        while (attempts < MAX_RETRIES && !success) {
            try {
                attempts++;
                dispatchWebhook(eventType, payload);
                success = true;
                log.info("Webhook emitted successfully for event: {}", eventType);
            } catch (Exception e) {
                log.error("Failed to emit webhook on attempt {} for event {}: {}", attempts, eventType, e.getMessage());
                if (attempts == MAX_RETRIES) {
                    log.error("Max retries reached for webhook event: {}. Giving up.", eventType);
                } else {
                    try {
                        // Exponential backoff with jitter
                        long jitter = (long) (Math.random() * 1000);
                        Thread.sleep(backoffDelay + jitter);
                        backoffDelay *= 2; // double the delay for next retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("Retry sleep interrupted", ie);
                        break;
                    }
                }
            }
        }
    }

    private void dispatchWebhook(String eventType, PaymentResult payload) throws Exception {
        // Example merchant webhook URL - in real usage, this should be looked up dynamically
        String webhookUrl = "https://merchant.example.com/webhook";

        URL url = new URL(webhookUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Serialize payload to JSON - assuming PaymentResult has a suitable toJson() method
        String jsonPayload = payload.toJson();

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new Exception("Failed to dispatch webhook. HTTP response code: " + responseCode);
        }
    }
}
