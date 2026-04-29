package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBleAdvertiser
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.undecodableState

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class ConnectWithHolderDeviceScreenTest {

    @get:Rule
    val composeTestRule = ConnectWithHolderDeviceRule(createComposeRule())

    private lateinit var testViewModel: SessionEstablishmentViewModel

    private val logger = SystemLogger()

    fun createViewModel(): SessionEstablishmentViewModel = SessionEstablishmentViewModel(
        logger = logger,
        orchestrator = FakeOrchestrator(),
        advertiser = FakeBleAdvertiser(),
    )

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun `does not attempt to open system Bluetooth alert when permissions are not granted`() =
        runTest {
            testViewModel = createViewModel()

            composeTestRule.run {
                render(
                    undecodableState,
                    Modifier,
                    testViewModel,
                )
            }

            composeTestRule.waitForIdle()
            composeTestRule.assertBluetoothPromptIsNotDisplayed()
        }
}
