# Payments Core API Documentation

## Overview
The Payments Core library is the backbone of NovaPay's payment processing. It manages the payment lifecycle, ensuring that transactions are processed efficiently and accurately.

## Key Components
- **PaymentService**: Orchestrates the complete payment flow.
- **TransactionEngine**: Manages the state machine for transactions, ensuring they follow the correct workflow.
- **IdempotencyHandler**: Prevents duplicate processing of payment requests by deduplicating concurrent and retried requests.
- **WebhookService**: Emits events to merchant webhooks, allowing for real-time updates.

## Setup Instructions
To set up the Payments Core library, run the following commands:
```bash
./gradlew build
./gradlew test
```

## Usage
Refer to the individual component classes for specific usage instructions.