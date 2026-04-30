package uk.gov.onelogin.sharing.orchestration.holder.credential

class FakeCredentialRequestHandler : CredentialRequestHandler {
    var resultToReturn: ValidatedCredential? = null
    var exceptionToThrow: CredentialRequestException? = null

    override suspend fun requestAndValidate(requestedDocType: String): ValidatedCredential {
        exceptionToThrow?.let { throw it }
        return resultToReturn
            ?: throw CredentialRequestException("No result configured")
    }
}
