package uk.gov.onelogin.sharing.orchestration.holder.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentDto

@ContributesBinding(scope = AppScope::class, binding = binding<InboundMessageClassifier>())
class InboundMessageClassifierImpl(private val logger: Logger) : InboundMessageClassifier {
    override fun getMessageType(rawBytes: ByteArray): InboundMessageType {
        val tree = try {
            CborMapper.default.readTree(rawBytes)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.debug(logTag, "Inbound message is not valid CBOR")
            return InboundMessageType.Unknown
        }

        // SessionEstablishment has eReaderKey + data
        if (tree.has(SessionEstablishmentDto.E_READER_KEY_KEY) &&
            tree.has(SessionEstablishmentDto.DATA_KEY)
        ) {
            logger.debug(logTag, "Classified inbound message as SessionEstablishment")
            return InboundMessageType.SessionEstablishment
        }

        // Status-only SessionData has status but no data
        if (!tree.has(KEY_DATA) && tree.has(KEY_STATUS)) {
            val statusCode = tree[KEY_STATUS].asInt().toUInt()
            SessionDataStatus.from(statusCode)?.let { status ->
                logger.debug(logTag, "Classified inbound message as status-only: $status")
                return InboundMessageType.StatusOnly(status)
            }
        }

        logger.debug(logTag, "Inbound message does not match any known type")
        return InboundMessageType.Unknown
    }

    private companion object {
        const val KEY_DATA = "data"
        const val KEY_STATUS = "status"
    }
}
