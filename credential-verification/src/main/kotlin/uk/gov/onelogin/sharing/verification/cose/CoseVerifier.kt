package uk.gov.onelogin.sharing.verification.cose

/**
 * Public contract for COSE verification as defined in the C2-C9 implementation plan.
 */
fun interface CoseVerifier {
    /**
     * Verifies a COSE Sign1 structure based on the provided request.
     *
     * @param request The verification request details (Attached, Detached, or KeyBased).
     * @return The result of the verification, specific to the request type.
     * @throws CoseVerificationFailure if verification fails.
     */
    fun verify(request: CoseVerificationRequest): CoseVerificationResult
}
