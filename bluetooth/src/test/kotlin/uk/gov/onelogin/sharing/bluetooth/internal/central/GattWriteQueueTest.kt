package uk.gov.onelogin.sharing.bluetooth.internal.central

import java.util.UUID
import kotlin.test.assertEquals
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class GattWriteQueueTest {
    private val targetUuid = UUID.randomUUID()
    private val otherUuid = UUID.randomUUID()
    private val queue = GattWriteQueue(targetUuid)

    @Test
    fun `awaitWriteConfirmation returns true when onWriteComplete signals success`() = runTest {
        launch { queue.onWriteComplete(targetUuid, true) }
        assertEquals(true, queue.awaitWriteConfirmation())
    }

    @Test
    fun `awaitWriteConfirmation returns false when onWriteComplete signals failure`() = runTest {
        launch { queue.onWriteComplete(targetUuid, false) }
        assertEquals(false, queue.awaitWriteConfirmation())
    }

    @Test
    fun `onWriteComplete does not signal when uuid does not match targetUuid`() = runTest {
        queue.onWriteComplete(otherUuid, true)

        launch { queue.onWriteComplete(targetUuid, true) }
        assertEquals(true, queue.awaitWriteConfirmation())
    }

    @Test
    fun `awaitWriteConfirmation suspends until onWriteComplete is called`() = runTest {
        var signalled = false
        launch {
            queue.awaitWriteConfirmation()
            signalled = true
        }
        assertEquals(false, signalled)
        queue.onWriteComplete(targetUuid, true)
    }
}
