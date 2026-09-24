package uk.gov.onelogin.sharing.verification.reader

/**
 * Public contract for extracting and validating the DVS Privacy Policy URL
 * from the Reader leaf certificate.
 */
fun interface ValidatePrivacyPolicyUseCase {
    /**
     * Extracts and validates the DVS Privacy Policy URL and organizationName
     * from the leaf certificate in [verifiedReaderRequest].
     *
     * @param verifiedReaderRequest The verified request containing the candidate [DocRequest]
     * and verified Reader leaf certificate.
     * @return [AuthenticatedReaderRequest] containing the candidate [DocRequest],
     * parsed privacy policy [android.net.Uri], and raw organizationName.
     * @throws ReaderAuthenticationFailure with reason [ReaderAuthenticationReason.PRIVACY_POLICY_URL_INVALID]
     * if validation fails.
     */
    fun validate(verifiedReaderRequest: VerifiedReaderRequest): AuthenticatedReaderRequest
}
