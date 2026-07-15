package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlin.jvm.java
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponseDto
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.validity.IssuerAuthResult

@Inject
@ContributesBinding(
    scope = CredentialVerificationScope::class,
    binding = binding<MsoFieldVerifier>()
)
class MsoFieldVerifierImpl : MsoFieldVerifier {

    private val cborMapper = ObjectMapper(CBORFactory())

    override fun verify(
        document: VerifiableDocument,
        mso: MobileSecurityObject,
        issuerAuthResult: IssuerAuthResult
    ) {
        val majorVersion = mso.version.substringBefore('.')
        if (majorVersion != MAJOR_VERSION) {
            throw VerificationResult.Failure(VerificationError.INVALID_MSO_VERSION)
        }

        if (mso.docType != MobileSecurityObject.DOC_TYPE || mso.docType != document.docType) {
            throw VerificationResult.Failure(VerificationError.INVALID_DOC_TYPE)
        }

        if (mso.digestAlgorithm != MobileSecurityObject.MSO_DIGEST_ALGORITHM) {
            throw VerificationResult.Failure(VerificationError.UNSUPPORTED_DIGEST_ALGORITHM)
        }

        verifyIssuingCountry(document, issuerAuthResult)
        verifyIssuingJurisdiction(document, issuerAuthResult)
    }

    private fun verifyIssuingCountry(
        document: VerifiableDocument,
        issuerAuthResult: IssuerAuthResult
    ) {
        val elementValue = findElementValue(document, ELEMENT_ISSUING_COUNTRY) ?: return
        if (elementValue != issuerAuthResult.subjectCountry) {
            throw VerificationResult.Failure(VerificationError.INVALID_MSO)
        }
    }

    private fun verifyIssuingJurisdiction(
        document: VerifiableDocument,
        issuerAuthResult: IssuerAuthResult
    ) {
        val subjectState = issuerAuthResult.subjectState ?: return
        val elementValue = findElementValue(document, ELEMENT_ISSUING_JURISDICTION) ?: return
        if (elementValue != subjectState) {
            throw VerificationResult.Failure(VerificationError.INVALID_MSO)
        }
    }

    private fun findElementValue(document: VerifiableDocument, identifier: String): String? {
        val items = document.issuerSigned.nameSpaces
            ?.get(MobileSecurityObject.NAMESPACE) ?: return null
        for (itemBytes in items) {
            val value = decodeElementValue(itemBytes, identifier)
            if (value != null) return value
        }
        return null
    }

    private fun decodeElementValue(itemBytes: ByteArray, identifier: String): String? = try {
        val inner = CborMapper.default.readValue(itemBytes, EmbeddedCbor::class.java).encoded
        val item = cborMapper.readValue(
            inner,
            DeviceResponseDto.IssuerSignedItemDTO::class.java
        )
        if (item.elementIdentifier == identifier) {
            item.elementValue.toString()
        } else {
            null
        }
    } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
        null
    }

    private companion object {
        const val MAJOR_VERSION = "1"
        const val ELEMENT_ISSUING_COUNTRY = "issuing_country"
        const val ELEMENT_ISSUING_JURISDICTION = "issuing_jurisdiction"
    }
}
