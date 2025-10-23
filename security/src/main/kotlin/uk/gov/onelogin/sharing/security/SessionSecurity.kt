package uk.gov.onelogin.sharing.security

import java.security.PublicKey

interface SessionSecurity {
    val publicKey: PublicKey
}
