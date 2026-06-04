package uk.gov.onelogin.sharing.verification.trust

import java.security.cert.X509Certificate

internal fun orderCertificates(certs: List<X509Certificate>): List<X509Certificate> {
    if (certs.size <= 1) return certs

    val skiToCert = mutableMapOf<String, X509Certificate>()
    certs.forEach { cert ->
        cert.subjectKeyIdentifierHex()?.let { skiToCert[it] = cert }
    }

    val referencedSkis = certs.mapNotNull { it.authorityKeyIdentifierHex() }.toSet()

    val leaf = certs.find { cert ->
        val ski = cert.subjectKeyIdentifierHex() ?: return@find false
        ski !in referencedSkis
    } ?: certs.first()

    val ordered = mutableListOf(leaf)
    var current = leaf
    var parent = current.nextParent(skiToCert, ordered)
    while (ordered.size < certs.size && parent != null) {
        ordered.add(parent)
        current = parent
        parent = current.nextParent(skiToCert, ordered)
    }

    return ordered
}

private fun X509Certificate.nextParent(
    skiToCert: Map<String, X509Certificate>,
    visited: List<X509Certificate>
): X509Certificate? {
    val aki = authorityKeyIdentifierHex() ?: return null
    val parent = skiToCert[aki] ?: return null
    return parent.takeUnless { it in visited }
}
