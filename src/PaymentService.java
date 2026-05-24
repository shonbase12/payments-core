// Implement input validation logic for PaymentRequest
public boolean isValidPaymentRequest(PaymentRequest request) {
    if (request == null || request.getIdempotencyKey() == null) {
        return false;
    }
    // Add more validation checks as needed
    return true;
}