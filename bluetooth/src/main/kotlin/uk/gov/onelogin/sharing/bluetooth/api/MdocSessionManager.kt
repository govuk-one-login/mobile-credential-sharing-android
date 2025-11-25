package uk.gov.onelogin.sharing.bluetooth.api

import java.util.UUID
import kotlinx.coroutines.flow.StateFlow

interface MdocSessionManager {
    val state: StateFlow<MdocSessionState>

    suspend fun start(serviceUuid: UUID)

    suspend fun stop()
}
