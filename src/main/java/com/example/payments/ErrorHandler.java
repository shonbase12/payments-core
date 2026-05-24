// Updated error handling logic
try {
    // existing logic
} catch (PaymentException e) {
    // handle PaymentException
} catch (Exception e) {
    // handle other exceptions
    log.error("Unexpected error occurred: ", e);
}