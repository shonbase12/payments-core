package com.novapay.payments;

import com.novapay.payments.model.PaymentRequest;
import com.novapay.payments.model.PaymentResult;
import com.novapay.payments.model.TransactionState;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TransactionEngine {
    private final Cache<String, PaymentResult> cache;

    public TransactionEngine() {
        this.cache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();
    }

    public PaymentResult execute(PaymentRequest request) {
        String cacheKey = request.getId();
        PaymentResult cachedResult = cache.getIfPresent(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }
        TransactionState state = TransactionState.PENDING;
        try {
            state = TransactionState.PROCESSING;
            String processorRef = callProcessor(request);
            state = TransactionState.COMPLETED;
            PaymentResult result = PaymentResult.success(processorRef);
            cache.put(cacheKey, result);
            return result;
        } catch (PaymentException e) {
            state = TransactionState.FAILED;
            throw e;
        }
    }

    public List<PaymentResult> executeBatch(List<PaymentRequest> requests) {
        return requests.parallelStream() // Use parallel stream for concurrent processing
            .map(this::execute)
            .toList();
    }

    private String callProcessor(PaymentRequest request) {
        throw new UnsupportedOperationException("Processor not configured");
    }
}