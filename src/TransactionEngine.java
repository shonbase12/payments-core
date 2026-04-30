package com.novapay.payments;

import com.novapay.payments.model.PaymentRequest;
import com.novapay.payments.model.PaymentResult;
import com.novapay.payments.model.TransactionState;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionEngine {
    private final Cache<String, PaymentResult> cache;
    private final ReentrantLock lock = new ReentrantLock();
    private static final Logger log = LoggerFactory.getLogger(TransactionEngine.class);

    public TransactionEngine() {
        this.cache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();
    }

    public PaymentResult execute(PaymentRequest request) {
        String cacheKey = request.getId();

        // First check the cache without locking
        PaymentResult cachedResult = cache.getIfPresent(cacheKey);
        if (cachedResult != null) {
            log.debug("Cache hit for transaction",
                      kv("transactionId", cacheKey));
            return cachedResult;
        }

        // Cache miss, acquire lock and double-check
        lock.lock();
        try {
            cachedResult = cache.getIfPresent(cacheKey);
            if (cachedResult != null) {
                log.debug("Cache hit after lock for transaction",
                          kv("transactionId", cacheKey));
                return cachedResult;
            }
        } finally {
            lock.unlock();
        }

        TransactionState state = TransactionState.PENDING;
        try {
            log.info("Starting transaction processing",
                     kv("transactionId", cacheKey),
                     kv("state", state));
            state = TransactionState.PROCESSING;
            String processorRef = callProcessor(request);
            state = TransactionState.COMPLETED;
            log.info("Transaction completed successfully",
                     kv("transactionId", cacheKey),
                     kv("state", state),
                     kv("processorRef", processorRef));

            PaymentResult result = PaymentResult.success(processorRef);

            lock.lock();
            try {
                cache.put(cacheKey, result);
                log.debug("Cached transaction result",
                          kv("transactionId", cacheKey));
            } finally {
                lock.unlock();
            }

            return result;
        } catch (PaymentException e) {
            state = TransactionState.FAILED;
            log.error("PaymentException during processing",
                      kv("transactionId", cacheKey),
                      kv("state", state),
                      kv("error", e.getMessage()));
            throw e;
        } catch (Exception e) {
            state = TransactionState.FAILED;
            log.error("Unexpected exception during processing",
                      kv("transactionId", cacheKey),
                      kv("state", state),
                      kv("error", e.getMessage()),
                      e);
            throw new PaymentException("Transaction processing failed", "PROCESSOR_ERROR");
        }
    }

    public List<PaymentResult> executeBatch(List<PaymentRequest> requests) {
        return requests.parallelStream()
            .map(this::execute)
            .toList();
    }

    private String callProcessor(PaymentRequest request) {
        try {
            Thread.sleep(100);
            return "PROC_REF_" + request.getId();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentException("Processor interrupted", "PROCESSOR_INTERRUPTED");
        } catch (Exception e) {
            throw new PaymentException("Processor error: " + e.getMessage(), "PROCESSOR_ERROR");
        }
    }

    private Object kv(String key, Object value) {
        return new Object() {
            @Override
            public String toString() {
                return key + "=" + value;
            }
        };
    }
}
