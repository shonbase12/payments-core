package com.novapay.payments.idempotency;

import com.novapay.payments.model.PaymentResult;
import java.util.concurrent.ConcurrentHashMap;

// TODO: replace with Redis-backed store for multi-instance support
public class IdempotencyHandler {
    private final ConcurrentHashMap<String, PaymentResult> store = new ConcurrentHashMap<>();

    public PaymentResult get(String key) {
        return store.get(key);
    }

    public void store(String key, PaymentResult result) {
        store.put(key, result);
    }
}
