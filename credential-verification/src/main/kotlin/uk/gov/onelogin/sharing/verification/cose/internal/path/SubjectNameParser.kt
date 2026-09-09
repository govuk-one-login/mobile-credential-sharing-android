package uk.gov.onelogin.sharing.verification.cose.internal.path

import java.security.cert.X509Certificate

internal const val OID_COUNTRY = "2.5.4.6"
internal const val OID_STATE_OR_PROVINCE = "2.5.4.8"
internal const val OID_ORG = "2.5.4.10"
private const val COUNTRY = "C"
private const val STATE = "ST"
private const val ORG = "O"
private const val RFC_2253 = "RFC2253"

internal fun parseSubjectName(cert: X509Certificate): Map<String, String> {
    val result = mutableMapOf<String, String>()
    val dn = cert.subjectX500Principal.getName(RFC_2253)
    for (rdn in splitRdns(dn)) {
        val eqIndex = rdn.indexOf('=')
        if (eqIndex < 0) continue
        val key = rdn.substring(0, eqIndex).trim()
        val value = rdn.substring(eqIndex + 1).trim()
        when (key) {
            COUNTRY, OID_COUNTRY -> result[OID_COUNTRY] = value
            STATE, OID_STATE_OR_PROVINCE -> result[OID_STATE_OR_PROVINCE] = value
            ORG, OID_ORG -> result[OID_ORG] = value
        }
    }
    return result
}

private fun splitRdns(dn: String): List<String> {
    val rdns = mutableListOf<String>()
    val current = StringBuilder()
    var i = 0
    while (i < dn.length) {
        when (val ch = dn[i]) {
            '\\' if i + 1 < dn.length -> {
                current.append(ch)
                current.append(dn[i + 1])
                i += 2
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
