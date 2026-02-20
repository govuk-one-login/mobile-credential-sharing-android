package uk.gov.onelogin.sharing.security.secureArea.publickey

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import java.security.KeyPair
import java.security.interfaces.ECPublicKey
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_ALGORITHM
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.secureArea.KeyGenerator

/**
 * [KeyGenerator.PublicKeyGenerator] implementation that calls the underlying [keyPairGenerator]
 * to create a [java.security.KeyPair].
 *
 * This implementation expects that the internally called [java.security.KeyPair.getPublic]
 * function returns an instance of [ECPublicKey].
 */
@ContributesBinding(ViewModelScope::class)
class EcPublicCoseKeyGenerator(private val logger: Logger) : KeyGenerator.PublicKeyGenerator {
    override fun getCoseKey(keyPair: KeyPair?): CoseKey = keyPair.let { it!!.public as ECPublicKey }
        .let(CoseKey.Companion::generateCoseKey)
        .also {
            logger.debug(
                logTag,
                "Converted EC public key to CoseKey: $it"
            )
        }
}
