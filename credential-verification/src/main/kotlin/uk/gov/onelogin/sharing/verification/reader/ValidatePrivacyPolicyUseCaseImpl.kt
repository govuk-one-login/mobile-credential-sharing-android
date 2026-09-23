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

private val OID_ENTERPRISE_PREFIX = byteArrayOf(0x2B, 0x06, 0x01, 0x04, 0x01)

/**
 * Production implementation of [ValidatePrivacyPolicyUseCase].
 *
 * Extracts and validates the DVS Privacy Policy URL from the Subject Information Access (SIA) extension
 * of the verified Reader leaf certificate, extracts the unvalidated organizationName, and discards the certificate.
 */
@Inject
@ContributesBinding(CredentialVerificationScope::class)
class ValidatePrivacyPolicyUseCaseImpl : ValidatePrivacyPolicyUseCase {

    override fun validate(verifiedReaderRequest: VerifiedReaderRequest): AuthenticatedReaderRequest {
        val leafCert = verifiedReaderRequest.readerCertificate

        val rawUrl = extractSiaPrivacyPolicyUrl(leafCert)
        val validUri = validateUrlRules(rawUrl)

        val orgName = extractOrganizationName(leafCert)

        return AuthenticatedReaderRequest(
            docRequest = verifiedReaderRequest.docRequest,
            privacyPolicyUrl = validUri,
            readerOrganizationName = orgName,
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

    private fun extractSiaPrivacyPolicyUrl(leafCert: X509Certificate): String {
        val extValue = leafCert.getExtensionValue(OID_SIA)
            ?: throw ReaderAuthenticationFailure(PRIVACY_POLICY_URL_INVALID)

        return try {
            val unwrappedOctets = unwrapOctetStrings(extValue)
                ?: throw ReaderAuthenticationFailure(PRIVACY_POLICY_URL_INVALID)

            findPrivacyPolicyUriInSia(unwrappedOctets)
                ?: throw ReaderAuthenticationFailure(PRIVACY_POLICY_URL_INVALID)
        } catch (e: ReaderAuthenticationFailure) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            throw ReaderAuthenticationFailure(PRIVACY_POLICY_URL_INVALID, e)
        }
    }

    private fun unwrapOctetStrings(data: ByteArray): ByteArray? {
        var current = parseAsn1OctetString(data) ?: return null
        if (current.isNotEmpty() && (current[0].toInt() and BYTE_MASK) == TAG_OCTET_STRING) {
            current = parseAsn1OctetString(current) ?: return null
        }
        return current
    }

    private fun findPrivacyPolicyUriInSia(siaData: ByteArray): String? {
        val oidIndex = indexOfBytes(siaData, OID_ENTERPRISE_PREFIX)
        if (oidIndex < 0) return null

        val start = oidIndex + OID_ENTERPRISE_PREFIX.size
        val limit = minOf(start + MAX_SEARCH_LOOKAHEAD, siaData.size - 2)
        for (i in start until limit) {
            if ((siaData[i].toInt() and BYTE_MASK) == TAG_URI_LOCATION) {
                return extractUriAtOffset(siaData, i)
            }
        }
        return null
    }

    private fun extractUriAtOffset(siaData: ByteArray, uriTagIndex: Int): String? {
        if (uriTagIndex + 1 >= siaData.size) return null

        val len = siaData[uriTagIndex + 1].toInt() and BYTE_MASK
        val start = uriTagIndex + 2
        if (start + len > siaData.size) return null

        return String(siaData, start, len, Charsets.UTF_8)
    }

    private fun indexOfBytes(source: ByteArray, target: ByteArray): Int {
        for (i in 0..source.size - target.size) {
            if (source.copyOfRange(i, i + target.size).contentEquals(target)) {
                return i
            }
        }
        return -1
    }

    private fun parseAsn1OctetString(data: ByteArray): ByteArray? {
        if (data.size < 2 || (data[0].toInt() and BYTE_MASK) != TAG_OCTET_STRING) return null
        val len = data[1].toInt() and BYTE_MASK
        val offset = if (len < 128) 2 else 2 + (len and 0x7F)
        if (offset > data.size) return null
        return data.copyOfRange(offset, data.size)
    }

    private fun validateUrlRules(rawUrl: String): Uri {
        if (!isRawUrlCompliant(rawUrl)) {
            throw ReaderAuthenticationFailure(PRIVACY_POLICY_URL_INVALID)
        }

        val javaUri = parseJavaUri(rawUrl)
            ?: throw ReaderAuthenticationFailure(PRIVACY_POLICY_URL_INVALID)

        if (!isJavaUriCompliant(javaUri)) {
            throw ReaderAuthenticationFailure(PRIVACY_POLICY_URL_INVALID)
        }

        return try {
            Uri.parse(rawUrl)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            throw ReaderAuthenticationFailure(PRIVACY_POLICY_URL_INVALID, e)
        }
    }

    private fun isRawUrlCompliant(rawUrl: String): Boolean {
        if (rawUrl.length > MAX_URL_LENGTH) return false
        if (rawUrl.any { (it.code !in 0..MAX_ASCII_CODE) } || rawUrl.contains(' ')) return false
        return !rawUrl.contains('@')
    }

    private fun parseJavaUri(rawUrl: String): java.net.URI? = try {
        java.net.URI(rawUrl)
    } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
        null
    }

    private fun isJavaUriCompliant(javaUri: java.net.URI): Boolean {
        if (!SCHEME_HTTPS.equals(javaUri.scheme, ignoreCase = true)) return false
        val host = javaUri.host
        if (host.isNullOrEmpty() || host.any { it.code !in 0..MAX_ASCII_CODE }) return false
        if (javaUri.userInfo != null) return false
        return javaUri.isAbsolute && !javaUri.scheme.isNullOrEmpty()
    }
}
