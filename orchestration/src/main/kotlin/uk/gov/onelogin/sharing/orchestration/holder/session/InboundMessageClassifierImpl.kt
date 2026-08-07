package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.core.SharingSessionScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentDto

/**
 * MVP scope constraint (Holder perspective): The only valid inbound messages from
 * the Verifier to the Holder are:
 * SessionEstablishment — exactly once, containing the initial DeviceRequest.
 * SessionData without a data payload — status-only termination.
 *
 * @see
 *   <a href="https://govukverify.atlassian.net/wiki/spaces/DCMAW/pages/6707577055">
 *   Session Termination - Behaviour</a>
 */
@ContributesBinding(scope = SharingSessionScope::class, binding = binding<InboundMessageClassifier>())
class InboundMessageClassifierImpl(private val logger: Logger) : InboundMessageClassifier {
    override fun getMessageType(rawBytes: ByteArray): InboundMessageType {
        val tree = try {
            CborMapper.default.readTree(rawBytes)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.debug(logTag, "Inbound message is not valid CBOR: ${e.message}")
            return InboundMessageType.Unknown
        }

        return when {
            tree.has(SessionEstablishmentDto.E_READER_KEY_KEY) &&
                tree.has(SessionEstablishmentDto.DATA_KEY) -> {
                logger.debug(logTag, "Classified inbound message as SessionEstablishment")
                InboundMessageType.SessionEstablishment
            }

            !tree.has(KEY_DATA) && tree.has(KEY_STATUS) -> {
                val statusCode = tree[KEY_STATUS].asInt().toUInt()
                SessionDataStatus.from(statusCode)?.let { status ->
                    logger.debug(
                        logTag,
                        "Classified inbound message " +
                            "as status-only: $status ($statusCode)"
                    )
                    InboundMessageType.StatusOnly(status)
                } ?: run {
                    logger.debug(logTag, "Inbound message does not match any known type")
                    InboundMessageType.Unknown
                }
            }

            else -> {
                logger.debug(logTag, "Inbound message does not match any known type")
                InboundMessageType.Unknown
            }
        }
    }

    private companion object {
        const val KEY_DATA = "data"
        const val KEY_STATUS = "status"
    }
}
