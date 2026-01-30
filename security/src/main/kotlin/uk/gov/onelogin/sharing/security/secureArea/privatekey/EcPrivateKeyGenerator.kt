package uk.gov.onelogin.sharing.security.secureArea.privatekey

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import java.security.interfaces.ECPrivateKey
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_ALGORITHM
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.secureArea.KeyGenerator

/**
 * [KeyGenerator.PrivateKeyGenerator] implementation that calls the underlying [keyPairGenerator]
 * to create a [java.security.KeyPair].
 *
 * This implementation expects that the generated [java.security.KeyPair.getPrivate] returns an
 * instance of [ECPrivateKey].
 */
@ContributesBinding(ViewModelScope::class)
class EcPrivateKeyGenerator(
    private val keyPairGenerator: KeyGenerator.KeyPairGenerator,
    private val logger: Logger
) : KeyGenerator.PrivateKeyGenerator {
    override fun getSessionPrivateKey(): ECPrivateKey = keyPairGenerator.generateEcKeyPair(
        ELLIPTIC_CURVE_ALGORITHM,
        ELLIPTIC_CURVE_PARAMETER_SPEC
    ).let {
        it!!.private as ECPrivateKey
    }.also {
        logger.debug(
            logTag,
            "Obtained EC Private key from KeyPairGenerator"
        )
    }
}
