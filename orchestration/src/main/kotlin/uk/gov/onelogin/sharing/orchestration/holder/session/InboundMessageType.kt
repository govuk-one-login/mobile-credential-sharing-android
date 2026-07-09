package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus

sealed class InboundMessageType {
    /** A valid `SessionEstablishment` message (contains `eReaderKey` + `data`). */
    data object SessionEstablishment : InboundMessageType()

    /** A status-only `SessionData` (no `data` field). This is a peer termination signal. */
    data class StatusOnly(val status: SessionDataStatus) : InboundMessageType()

    /** The message could not be classified as a known valid type. */
    data object Unknown : InboundMessageType()
}
