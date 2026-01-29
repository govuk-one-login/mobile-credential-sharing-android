package uk.gov.onelogin.sharing.security.secureArea.publickey

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_ALGORITHM
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.secureArea.KeyGenerator
import java.security.interfaces.ECPublicKey

/**
 * [KeyGenerator.PublicKeyGenerator] implementation that calls the underlying [keyPairGenerator]
 * to create a [java.security.KeyPair].
 *
 * This implementation expects that the internally called [java.security.KeyPair.getPublic]
 * function returns an instance of [ECPublicKey].
 */
@ContributesBinding(ViewModelScope::class)
class EcPublicCoseKeyGenerator(private val keyPairGenerator: KeyGenerator.KeyPairGenerator) :
    KeyGenerator.PublicKeyGenerator {
    override fun generateSessionPublicKey(): CoseKey = keyPairGenerator.generateEcKeyPair(
        ELLIPTIC_CURVE_ALGORITHM,
        ELLIPTIC_CURVE_PARAMETER_SPEC
    )
        .let { it!!.public as ECPublicKey }
        .let(CoseKey.Companion::generateCoseKey)
}
