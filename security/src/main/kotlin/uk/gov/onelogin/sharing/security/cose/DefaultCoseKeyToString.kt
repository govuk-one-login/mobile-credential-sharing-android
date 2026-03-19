package uk.gov.onelogin.sharing.security.cose

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.security.cbor.encodeCbor
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor

/**
 * [CoseKeyToString] implementation that internally pads the provided [CoseKey] via [EmbeddedCbor].
 */
@ContributesBinding(scope = AppScope::class)
class DefaultCoseKeyToString(private val logger: Logger) : CoseKeyToString {
    /**
     * @return A hexadecimal string. This is the [EmbeddedCbor] padding of the provided [CoseKey].
     */
    override fun convert(key: CoseKey): String = key
        .encodeCbor()
        .let(::EmbeddedCbor)
        .encodeCbor()
        .toHexString()
        .also {
            logger.debug(
                logTag,
                "Encoded public CoseKey into EReaderKeyBytes: $it"
            )
        }
}
