package uk.gov.onelogin.sharing.verification.reader

import android.net.Uri
import dev.zacsweers.metro.Inject

private const val MAX_URL_LENGTH = 2048
private const val SCHEME_HTTPS = "https"
private const val MAX_ASCII_CODE = 127

/** Validates a raw SIA URI string against the DVS privacy policy URL rules. */
@Inject
class PrivacyPolicyUrlValidator {

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
