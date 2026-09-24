package uk.gov.onelogin.sharing.verification.reader

import dev.zacsweers.metro.Inject
import java.security.cert.X509Certificate

/**
 * Parses X.500 Subject Distinguished Names (DN) according to RFC 2253 / RFC 4514.
 */
@Inject
class SubjectNameParser {

    fun extractOrganizationName(cert: X509Certificate): String? = try {
        val dn = cert.subjectX500Principal.getName("RFC2253")
        splitRdns(dn).firstNotNullOfOrNull { rdn ->
            val eqIndex = rdn.indexOf('=')
            if (eqIndex > 0) {
                val key = rdn.substring(0, eqIndex).trim()
                val value = rdn.substring(eqIndex + 1).trim()
                if (key == "O" || key == "2.5.4.10") unescapeRdnValue(value) else null
            } else {
                null
            }
        }
    } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
        null
    }

    private fun splitRdns(dn: String): List<String> {
        val rdns = mutableListOf<String>()
        val current = StringBuilder()
        var i = 0
        while (i < dn.length) {
            when (val ch = dn[i]) {
                '\\' -> if (i + 1 < dn.length) {
                    current.append(ch)
                    current.append(dn[i + 1])
                    i += 2
                } else {
                    current.append(ch)
                    i++
                }

                ',' -> {
                    rdns.add(current.toString().trim())
                    current.clear()
                    i++
                }

                else -> {
                    current.append(ch)
                    i++
                }
            }
        }
        if (current.isNotEmpty()) rdns.add(current.toString().trim())
        return rdns
    }

    private fun unescapeRdnValue(value: String): String =
        value.replace("\\,", ",").replace("\\=", "=").replace("\\\\", "\\")
}
