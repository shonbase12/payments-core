# payments-core

Core payment processing library for NovaPay. Handles payment lifecycle,
idempotency, retries, and integration with downstream payment processors.

## Key components
- `PaymentService` — orchestrates the full payment flow, managing the initiation, processing, and confirmation of payments. It handles various payment scenarios and integrates with payment gateways.
- `TransactionEngine` — manages the transaction state machine, ensuring accurate tracking of transaction states such as pending, completed, failed, and refunded. This component allows for robust transaction management.
- `IdempotencyHandler` — deduplicates concurrent and retried payment requests, preventing double processing of payments and ensuring that each transaction is unique and handled correctly.
- `WebhookService` — emits events to merchant webhooks, allowing merchants to subscribe to payment updates, status changes, and other relevant notifications.

## Setup
```bash
./gradlew build
./gradlew test
```

## Dependencies
- Java 11 or higher
- Gradle 6.0 or higher

## Troubleshooting
- If you encounter issues during the build process, ensure that your Java and Gradle versions meet the requirements above.
- Common errors may include dependency resolution failures; check your internet connection and repository access.

## Contributing
We welcome contributions! Please follow these steps:
1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Submit a pull request detailing your changes.

## Contact
For support, please reach out to [support@novapay.com](mailto:support@novapay.com) or visit our [support forum](http://forum.novapay.com).