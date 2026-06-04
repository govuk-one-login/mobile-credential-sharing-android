package uk.gov.onelogin.sharing.verification.trust

import java.security.cert.X509Certificate

internal const val OID_COUNTRY = "2.5.4.6"
internal const val OID_STATE_OR_PROVINCE = "2.5.4.8"

internal fun parseSubjectName(cert: X509Certificate): Map<String, String> {
    val result = mutableMapOf<String, String>()
    val dn = cert.subjectX500Principal.getName("RFC2253")
    for (rdn in dn.split(",")) {
        val trimmed = rdn.trim()
        val eqIndex = trimmed.indexOf('=')
        if (eqIndex < 0) continue
        val key = trimmed.substring(0, eqIndex).trim()
        val value = trimmed.substring(eqIndex + 1).trim()
        when (key) {
            "C", OID_COUNTRY -> result[OID_COUNTRY] = value
            "ST", OID_STATE_OR_PROVINCE -> result[OID_STATE_OR_PROVINCE] = value
        }
    }
    return result
}
