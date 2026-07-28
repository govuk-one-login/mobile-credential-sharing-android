package uk.gov.onelogin.sharing.orchestration.verifier.session

import java.util.UUID

fun interface SessionTerminator {
    suspend fun terminate(
        serviceUuid: UUID?,
        bleOpen: Boolean,
        holderRequestedTermination: Boolean,
        sendSessionData: Boolean
    )
}
