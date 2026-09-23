package uk.gov.onelogin.sharing.verification.reader

import android.net.Uri
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.reader.ReaderAuthenticationReason.PRIVACY_POLICY_URL_INVALID

private const val OID_SIA = "1.3.6.1.5.5.7.1.11"
private const val MAX_URL_LENGTH = 2048
private const val SCHEME_HTTPS = "https"

private const val TAG_OCTET_STRING = 0x04
private const val TAG_URI_LOCATION = 0x86
private const val BYTE_MASK = 0xFF
private const val MAX_ASCII_CODE = 127
private const val MAX_SEARCH_LOOKAHEAD = 20
private const val ASN1_LENGTH_LONG_FORM_THRESHOLD = 128
private const val ASN1_LENGTH_VALUE_MASK = 0x7F

@Suppress("MagicNumber")
private val OID_ENTERPRISE_PREFIX = byteArrayOf(0x2B, 0x06, 0x01, 0x04, 0x01)

/**
 * Implementation of [ValidatePrivacyPolicyUseCase].
 *
 * Extracts and validates the DVS Privacy Policy URL from the Subject Information Access (SIA) extension
 * of the verified Reader leaf certificate, extracts the unvalidated organizationName, and discards the certificate.
 */
@Inject
@ContributesBinding(CredentialVerificationScope::class)
class ValidatePrivacyPolicyUseCaseImpl : ValidatePrivacyPolicyUseCase {

    override fun validate(
        verifiedReaderRequest: VerifiedReaderRequest
    ): AuthenticatedReaderRequest {
        val leafCert = verifiedReaderRequest.readerCertificate

        val rawUrl = SiaExtensionParser.extractPrivacyPolicyUrl(leafCert)
            ?: throw ReaderAuthenticationFailure(PRIVACY_POLICY_URL_INVALID)
        val validUri = PrivacyPolicyUrlValidator.validate(rawUrl)
            ?: throw ReaderAuthenticationFailure(PRIVACY_POLICY_URL_INVALID)

        return AuthenticatedReaderRequest(
            docRequest = verifiedReaderRequest.docRequest,
            privacyPolicyUrl = validUri,
            readerOrganizationName = extractOrganizationName(leafCert)
        )
    }

    private fun extractOrganizationName(cert: X509Certificate): String? = try {
        val dn = cert.subjectX500Principal.getName("RFC2253")
        dn.split(',').map { it.trim() }.firstNotNullOfOrNull { rdn ->
            if (rdn.startsWith("O=") || rdn.startsWith("2.5.4.10=")) {
                rdn.substring(rdn.indexOf('=') + 1).trim()
            } else {
                null
            }
        }
    } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
        null
    }
}

/**
 * Pulls the DVS Privacy Policy URI out of the SIA extension's DER encoding.
 * Returns null on any malformed/missing data rather than throwing, so the caller
 * decides how to surface the failure.
 */
private object SiaExtensionParser {

    fun extractPrivacyPolicyUrl(cert: X509Certificate): String? {
        val extValue = cert.getExtensionValue(OID_SIA) ?: return null
        return try {
            unwrapOctetStrings(extValue)?.let { findPrivacyPolicyUri(it) }
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            null
        }
    }

    private fun unwrapOctetStrings(data: ByteArray): ByteArray? {
        val outer = parseAsn1OctetString(data) ?: return null
        val isNestedOctetString =
            outer.isNotEmpty() && (outer[0].toInt() and BYTE_MASK) == TAG_OCTET_STRING
        return if (isNestedOctetString) parseAsn1OctetString(outer) else outer
    }

    private fun findPrivacyPolicyUri(siaData: ByteArray): String? {
        val oidIndex = indexOfBytes(siaData, OID_ENTERPRISE_PREFIX)
        if (oidIndex < 0) return null

        val start = oidIndex + OID_ENTERPRISE_PREFIX.size
        val limit = minOf(start + MAX_SEARCH_LOOKAHEAD, siaData.size - 2)
        val uriTagIndex =
            (start until limit).firstOrNull {
                (siaData[it].toInt() and BYTE_MASK) ==
                    TAG_URI_LOCATION
            }

        return uriTagIndex?.let { extractUriAtOffset(siaData, it) }
    }

    private fun extractUriAtOffset(siaData: ByteArray, uriTagIndex: Int): String? {
        if (uriTagIndex + 1 >= siaData.size) return null

        val len = siaData[uriTagIndex + 1].toInt() and BYTE_MASK
        val start = uriTagIndex + 2
        return if (start + len > siaData.size) null else String(siaData, start, len, Charsets.UTF_8)
    }

    private fun indexOfBytes(source: ByteArray, target: ByteArray): Int {
        val matchIndex = (0..source.size - target.size).firstOrNull { i ->
            source.copyOfRange(i, i + target.size).contentEquals(target)
        }
        return matchIndex ?: -1
    }

    private fun parseAsn1OctetString(data: ByteArray): ByteArray? {
        if (data.size < 2 || (data[0].toInt() and BYTE_MASK) != TAG_OCTET_STRING) return null

        val len = data[1].toInt() and BYTE_MASK
        val offset = if (len < ASN1_LENGTH_LONG_FORM_THRESHOLD) {
            2
        } else {
            2 + (len and ASN1_LENGTH_VALUE_MASK)
        }
        return if (offset > data.size) null else data.copyOfRange(offset, data.size)
    }
}

/** Validates a raw SIA URI string against the DVS privacy policy URL rules. */
private object PrivacyPolicyUrlValidator {

    fun validate(rawUrl: String): Uri? {
        val isCompliant = isRawUrlCompliant(rawUrl) &&
            parseJavaUri(rawUrl)?.let { isJavaUriCompliant(it) } == true

        if (!isCompliant) return null

        return try {
            Uri.parse(rawUrl)
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            null
        }
    }

    private fun isRawUrlCompliant(rawUrl: String): Boolean {
        val isAsciiNoSpaces = rawUrl.all { it.code in 0..MAX_ASCII_CODE } && !rawUrl.contains(' ')
        return rawUrl.length <= MAX_URL_LENGTH && isAsciiNoSpaces && !rawUrl.contains('@')
    }

    private fun parseJavaUri(rawUrl: String): java.net.URI? = try {
        java.net.URI(rawUrl)
    } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
        null
    }

    private fun isJavaUriCompliant(javaUri: java.net.URI): Boolean {
        val host = javaUri.host
        val hasValidHost = !host.isNullOrEmpty() && host.all { it.code in 0..MAX_ASCII_CODE }
        val isHttpsWithoutUserInfo = SCHEME_HTTPS.equals(javaUri.scheme, ignoreCase = true) &&
            javaUri.userInfo == null
        return hasValidHost && isHttpsWithoutUserInfo && javaUri.isAbsolute
    }
}
