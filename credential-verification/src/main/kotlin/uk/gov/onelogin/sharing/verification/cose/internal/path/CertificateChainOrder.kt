package uk.gov.onelogin.sharing.verification.cose.internal.path

import java.security.cert.X509Certificate

/**
 * Orders a list of certificates such that each certificate is followed by its issuer.
 * The first certificate in the resulting list is the leaf.
 */
internal fun orderCertificates(certs: List<X509Certificate>): List<X509Certificate> {
    if (certs.size <= 1) return certs

    val certMap = certs.associateBy { it.subjectX500Principal }
    val issuers = certs.map { it.issuerX500Principal }.toSet()
    val leaf = certs.find { it.subjectX500Principal !in issuers } ?: certs.first()

    val ordered = mutableListOf(leaf)
    var current = leaf
    while (true) {
        val issuer = certMap[current.issuerX500Principal]
        if (issuer == null || issuer == current) break
        ordered.add(issuer)
        current = issuer
    }
    return ordered
}
