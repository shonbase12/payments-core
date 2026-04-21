# Payments Core API Documentation

## Versioning
This documentation is for **Payments Core API v1.0**. For previous versions, please refer to the [changelog](CHANGELOG.md).

## Overview
The Payments Core library is the backbone of NovaPay's payment processing. It manages the payment lifecycle, ensuring that transactions are processed efficiently and accurately.

## Authentication
To access the Payments Core API, you must include an API key in the Authorization header of your requests:
```
Authorization: Bearer YOUR_API_KEY
```

## Key Components
- **PaymentService**: Orchestrates the complete payment flow.
- **TransactionEngine**: Manages the state machine for transactions, ensuring they follow the correct workflow.
- **IdempotencyHandler**: Prevents duplicate processing of payment requests by deduplicating concurrent and retried requests.
- **WebhookService**: Emits events to merchant webhooks, allowing for real-time updates.

## Error Handling
The API returns standard HTTP status codes to indicate the success or failure of a request:
- **200 OK**: The request was successful.
- **400 Bad Request**: The request was invalid or cannot be processed.
- **401 Unauthorized**: Authentication failed or missing.
- **404 Not Found**: The requested resource could not be found.
- **500 Internal Server Error**: An error occurred on the server side.

Example error response:
```json
{
    "error": {
        "code": 400,
        "message": "Invalid payment request"
    }
}
```

## Setup Instructions
To set up the Payments Core library, run the following commands:
```bash
./gradlew build
./gradlew test
```

## Usage
### PaymentService
```java
PaymentService paymentService = new PaymentService();
PaymentResponse response = paymentService.processPayment(paymentRequest);
```
### TransactionEngine
```java
TransactionEngine transactionEngine = new TransactionEngine();
Transaction transaction = transactionEngine.startTransaction();
```
### IdempotencyHandler
```java
IdempotencyHandler idempotencyHandler = new IdempotencyHandler();
idempotencyHandler.handleRequest(request);
```
### WebhookService
```java
WebhookService webhookService = new WebhookService();
webhookService.emitEvent(event);
```

## API Endpoints
### POST /payments
- **Description**: Initiates a payment transaction.
- **Request Body**:
    ```json
    {
        "amount": "number",
        "currency": "string",
        "paymentMethod": "string"
    }
    ```
- **Response**:
    ```json
    {
        "transactionId": "string",
        "status": "string"
    }
    ```

### GET /transactions/{id}
- **Description**: Retrieves the status of a transaction.
- **Path Parameters**:
    - `id` (string): The ID of the transaction to retrieve.
- **Response**:
    ```json
    {
        "transactionId": "string",
        "status": "string",
        "details": "object"
    }
    ```

### POST /webhooks
- **Description**: Receives webhook events from the payment gateway.
- **Request Body**:
    ```json
    {
        "event": "string",
        "data": "object"
    }
    ```
- **Response**:
    ```json
    {
        "received": true
    }
    ```

Refer to the individual component classes for specific usage instructions. For further assistance, please contact our support team at support@novapay.com.
