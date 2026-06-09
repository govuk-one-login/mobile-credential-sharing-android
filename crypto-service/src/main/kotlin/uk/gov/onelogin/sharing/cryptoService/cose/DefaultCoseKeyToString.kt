package uk.gov.onelogin.sharing.cryptoService.cose

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cose.toDto
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor

/**
 * [CoseKeyToString] implementation that internally pads the provided [CoseKey] via [EmbeddedCbor].
 */
@ContributesBinding(scope = AppScope::class)
class DefaultCoseKeyToString(private val logger: Logger) : CoseKeyToString {
    /**
     * @return A hexadecimal string. This is the [EmbeddedCbor] padding of the provided [CoseKey].
     */
    override fun convert(key: CoseKey): String = EmbeddedCbor(key.toDto().toCbor())
        .toCbor()
        .toHexString()
        .also { logger.debug(logTag, "Encoded public CoseKey into EReaderKeyBytes: $it") }
}
