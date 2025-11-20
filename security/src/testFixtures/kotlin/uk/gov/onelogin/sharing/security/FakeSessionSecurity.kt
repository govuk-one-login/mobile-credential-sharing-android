package uk.gov.onelogin.sharing.security

import java.security.interfaces.ECPublicKey
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity

class FakeSessionSecurity(private val publicKey: ECPublicKey?) : SessionSecurity {
    override fun generateEcPublicKey(algorithm: String, parameterSpec: String): ECPublicKey? =
        publicKey
}
