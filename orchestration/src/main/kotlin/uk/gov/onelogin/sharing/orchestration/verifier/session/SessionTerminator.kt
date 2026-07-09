package uk.gov.onelogin.sharing.orchestration.verifier.session

import java.util.UUID
import kotlinx.coroutines.flow.StateFlow

interface SessionTerminator {
    suspend fun terminate(serviceUuid: UUID?, bleOpen: Boolean, holderRequestedTermination: Boolean)
}
