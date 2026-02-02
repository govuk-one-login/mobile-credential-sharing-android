package uk.gov.onelogin.sharing.security.secureArea.keypair

import java.security.KeyPair
import java.security.KeyPairGenerator
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidKeyPair
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidPrivateKey
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidPublicKey

object KeyPairGeneratorStubs {
    const val ALGORITHM = "EC"
    const val PARAMETER_SPEC = "secp256r1"
    const val UNSUPPORTED_PARAMETER_SPEC = "secp384r1"
    const val INVALID_ALGORITHM = "INVALID_ALGO"
    const val INVALID_SPEC = "INVALID_SPEC"

    val validKeyPair = generateValidKeyPair()
    val keyPairWithNullEntries = KeyPair(null, null)
    val keyPairWithPrivateKey = KeyPair(null, generateValidPrivateKey())
    val keyPairWithPublicKey = KeyPair(generateValidPublicKey(), null)
    val rsaKeyPair = KeyPairGenerator.getInstance("RSA").apply {
        initialize(1024)
    }.generateKeyPair()
}
