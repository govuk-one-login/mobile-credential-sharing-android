package uk.gov.onelogin.sharing.orchestration.holder.credential

import uk.gov.onelogin.sharing.core.SharingSessionScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.AgeOverNNRequestLimitException
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.AgeOverNNRequestLimitException.Companion.MESSAGE
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.FilterIssuerSignedUseCase
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.NoMatchingAttributesException
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.ParsedRawCredential
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.RawCredentialParser
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.credential.RawCredentialParsingException
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.orchestration.Credential
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.CredentialRequest

@ContributesBinding(SharingSessionScope::class)
class CredentialRequestHandlerImpl(
    private val credentialProvider: CredentialProvider,
    private val rawCredentialParser: RawCredentialParser,
    private val filterIssuerSignedUseCase: FilterIssuerSignedUseCase
) : CredentialRequestHandler {

    override suspend fun requestAndValidate(
        requestedDocType: String,
        deviceRequest: DeviceRequest
    ): CredentialRequestResult {
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
        return parseValidateAndFilter(selected, requestedDocType, deviceRequest)
    }

    private fun parseValidateAndFilter(
        credential: Credential,
        requestedDocType: String,
        deviceRequest: DeviceRequest
    ): CredentialRequestResult {
        val parsed = parseCredential(credential)
        val validatedCredential = validateDocType(credential.id, parsed, requestedDocType)
        val filteredIssuerSigned = try {
            filterIssuerSignedUseCase.filter(
                ParsedRawCredential(
                    nameSpaces = parsed.nameSpaces,
                    issuerAuth = parsed.issuerAuth,
                    msoDocType = requestedDocType
                ),
                deviceRequest
            )
        } catch (e: NoMatchingAttributesException) {
            throw CredentialRequestException(e.message ?: LOG_NO_MATCHING_ATTRIBUTES, e)
        } catch (e: AgeOverNNRequestLimitException) {
            throw CredentialRequestException(e.message ?: MESSAGE, e)
        }
        return CredentialRequestResult(
            validatedCredential = validatedCredential,
            filteredIssuerSigned = filteredIssuerSigned
        )
    }

    private fun parseCredential(credential: Credential) = try {
        rawCredentialParser.parse(credential.rawCredential)
    } catch (e: RawCredentialParsingException) {
        throw CredentialRequestException(LOG_MSO_DECODE_ERROR, e)
    }

    private fun validateDocType(
        credentialId: String,
        parsed: ParsedRawCredential,
        requestedDocType: String
    ): ValidatedCredential {
        if (parsed.msoDocType != requestedDocType) {
            throw CredentialRequestException(LOG_DOCTYPE_MISMATCH)
        }
        return ValidatedCredential(
            credentialId = credentialId,
            nameSpaces = parsed.nameSpaces,
            issuerAuth = parsed.issuerAuth
        )
    }

    companion object {
        const val LOG_NO_MATCHING_ATTRIBUTES =
            "SessionData termination initiated due to no matching attributes"
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
