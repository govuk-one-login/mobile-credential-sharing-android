package uk.gov.onelogin.sharing.holder.mdoc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.holder.FakeBluetoothServerFactory

@RunWith(RobolectricTestRunner::class)
class MdocSessionManagerFactoryTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val fakeBluetoothServerFactory = FakeBluetoothServerFactory()
    private val fakeLogger = SystemLogger()
    private val testScope = CoroutineScope(SupervisorJob() + dispatcherRule.testDispatcher)

    @Test
    fun `creates an instance of MdocSessionManager`() {
        val sessionManager = MdocSessionManagerFactory(
            bluetoothFactory = fakeBluetoothServerFactory,
            logger = fakeLogger
        )
            .create(testScope)

        assertNotNull(sessionManager)
        assertTrue(sessionManager is AndroidMdocSessionManager)
    }
}
