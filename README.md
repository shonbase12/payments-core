# payments-core

Core payment processing library for NovaPay. Handles payment lifecycle,
idempotency, retries, and integration with downstream payment processors.

## Key components
- `PaymentService` — orchestrates the full payment flow
- `TransactionEngine` — manages transaction state machine
- `IdempotencyHandler` — deduplicates concurrent and retried payment requests
- `WebhookService` — emits events to merchant webhooks

## Setup
```bash
./gradlew build
./gradlew test
```
