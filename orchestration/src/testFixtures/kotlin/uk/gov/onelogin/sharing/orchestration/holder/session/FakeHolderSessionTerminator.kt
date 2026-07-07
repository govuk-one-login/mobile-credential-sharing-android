package uk.gov.onelogin.sharing.orchestration.holder.session

import java.util.UUID

class FakeHolderSessionTerminator : HolderSessionTerminator {
    var terminateCalls = 0
    var lastServiceUuid: UUID? = null

    override suspend fun terminate(serviceUuid: UUID) {
        terminateCalls++
        lastServiceUuid = serviceUuid
    }
}
