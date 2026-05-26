package uk.gov.onelogin.sharing.orchestration.holder.credential

import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned

data class CredentialRequestResult(
    val validatedCredential: ValidatedCredential,
    val filteredIssuerSigned: IssuerSigned
)
