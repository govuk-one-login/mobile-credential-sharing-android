package uk.gov.onelogin.sharing.security.secureArea.privatekey

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import java.security.interfaces.ECPrivateKey
import uk.gov.onelogin.sharing.security.engagement.EngagementAlgorithms
import uk.gov.onelogin.sharing.security.secureArea.KeyGenerator

/**
 * [KeyGenerator.PrivateKeyGenerator] implementation that calls the underlying [keyPairGenerator]
 * to create a [java.security.KeyPair].
 *
 * This implementation expects that the generated [java.security.KeyPair.getPrivate] returns an
 * instance of [ECPrivateKey].
 */
@ContributesBinding(ViewModelScope::class)
class EcPrivateKeyGenerator(private val keyPairGenerator: KeyGenerator.KeyPairGenerator) :
    KeyGenerator.PrivateKeyGenerator {
    override fun getSessionPrivateKey(): ECPrivateKey = keyPairGenerator.generateEcKeyPair(
        EngagementAlgorithms.EC_ALGORITHM,
        EngagementAlgorithms.EC_PARAMETER_SPEC
    )
        ?.private as ECPrivateKey
}
