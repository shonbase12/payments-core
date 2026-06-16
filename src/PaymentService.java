// Implement input validation logic for PaymentRequest
public boolean isValidPaymentRequest(PaymentRequest request) {
    if (request == null || request.getIdempotencyKey() == null) {
        return false;
    }
    if (request.getAmount() <= 0) {
        throw new IllegalArgumentException("Amount must be greater than zero.");
    }
    // Additional validation checks as needed
    return true;
}