package uk.gov.onelogin.sharing.orchestration.holder.credential

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.RawCredentialParser
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.RawCredentialParsingException
import uk.gov.onelogin.sharing.orchestration.Credential
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.CredentialRequest

@ContributesBinding(AppScope::class)
class CredentialRequestHandlerImpl(
    private val credentialProvider: CredentialProvider,
    private val rawCredentialParser: RawCredentialParser
) : CredentialRequestHandler {

    override suspend fun requestAndValidate(requestedDocType: String): ValidatedCredential {
        val credentials = try {
            credentialProvider.getCredentials(
                CredentialRequest(documentTypes = listOf(requestedDocType))
            )
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            throw CredentialRequestException(LOG_GET_CREDENTIALS_ERROR, e)
        }

        if (credentials.isEmpty()) {
            throw CredentialRequestException(LOG_NO_CREDENTIALS)
        }

        val selected = credentials.first()
        return parseAndValidate(selected, requestedDocType)
    }

    private fun parseAndValidate(
        credential: Credential,
        requestedDocType: String
    ): ValidatedCredential {
        val parsed = try {
            rawCredentialParser.parse(credential.rawCredential)
        } catch (e: RawCredentialParsingException) {
            throw CredentialRequestException(LOG_MSO_DECODE_ERROR, e)
        }

        if (parsed.msoDocType != requestedDocType) {
            throw CredentialRequestException(LOG_DOCTYPE_MISMATCH)
        }

        return ValidatedCredential(
            credentialId = credential.id,
            nameSpaces = parsed.nameSpaces,
            issuerAuth = parsed.issuerAuth
        )
    }

    companion object {
        const val LOG_NO_CREDENTIALS =
            "SessionData termination initiated due to getCredentials no credentials returned"
        const val LOG_GET_CREDENTIALS_ERROR =
            "SessionData termination initiated due to getCredentials error thrown"
        const val LOG_MSO_DECODE_ERROR =
            "SessionData termination initiated due to MSO decoding error"
        const val LOG_DOCTYPE_MISMATCH =
            "SessionData termination initiated due to " +
                "getCredentials no credentials of correct docType returned"
        const val LOG_DOCTYPE_MATCH =
            "provided credential matches DeviceRequest docType"
    }
}
