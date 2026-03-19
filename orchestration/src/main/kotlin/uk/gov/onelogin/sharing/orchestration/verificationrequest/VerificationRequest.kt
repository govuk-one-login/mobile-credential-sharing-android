package uk.gov.onelogin.sharing.orchestration.verificationrequest

data class VerificationRequest(val documentType: String, val requestedElements: List<String>) {
    companion object {
        fun typed(
            documentType: DocumentType,
            requestElements: List<RequestElement>
        ): VerificationRequest = VerificationRequest(
            documentType = documentType.value,
            requestedElements = requestElements.map { it.value }
        )

        fun raw(documentType: String, requestedElements: List<String>): VerificationRequest =
            VerificationRequest(
                documentType = documentType,
                requestedElements = requestedElements
            )
    }
}
