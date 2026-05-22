package uk.gov.onelogin.sharing.bluetooth.internal.central

import java.util.UUID

class FakeGattWriteQueue(var confirmationResult: Boolean = true) : GattWriteQueue {
    override suspend fun awaitWriteConfirmation(): Boolean = confirmationResult
    override fun onWriteComplete(characteristicUuid: UUID, success: Boolean) = Unit
}
