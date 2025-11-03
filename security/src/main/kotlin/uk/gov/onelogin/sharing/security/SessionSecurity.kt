package uk.gov.onelogin.sharing.security

import java.security.PublicKey

fun interface SessionSecurity {
    fun generateEcPublicKey(): PublicKey?
}
