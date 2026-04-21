package com.novapay.payments.idempotency;

import com.novapay.payments.model.PaymentResult;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class IdempotencyHandler {
    private final JedisPool jedisPool;

    public IdempotencyHandler(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public PaymentResult get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.get(key);
            return result != null ? deserialize(result) : null;
        }
    }

    public void store(String key, PaymentResult result) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, 3600, serialize(result)); // Set with 1 hour TTL
        }
    }

    private String serialize(PaymentResult result) {
        // Implement serialization logic here
    }

    private PaymentResult deserialize(String data) {
        // Implement deserialization logic here
    }
}