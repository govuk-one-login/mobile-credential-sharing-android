package uk.gov.onelogin.sharing.di.api.verifier

data class VerificationRequest(val documentType: String, val requestedElements: List<String>)
