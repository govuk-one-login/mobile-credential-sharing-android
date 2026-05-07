package uk.gov.onelogin.sharing.orchestration.holder.credential

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSigned

data class CredentialRequestResult(
    val validatedCredential: ValidatedCredential,
    val filteredIssuerSigned: IssuerSigned
)
