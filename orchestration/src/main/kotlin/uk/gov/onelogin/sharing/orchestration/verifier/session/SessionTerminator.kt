package uk.gov.onelogin.sharing.orchestration.verifier.session

import java.util.UUID
import kotlinx.coroutines.flow.StateFlow

interface SessionTerminator {
    val state: StateFlow<TerminationState>

    suspend fun terminate(serviceUuid: UUID?, bleOpen: Boolean, holderRequestedTermination: Boolean)

    companion object {
        const val TERMINATION_DELAY_MS = 500L
    }
}
