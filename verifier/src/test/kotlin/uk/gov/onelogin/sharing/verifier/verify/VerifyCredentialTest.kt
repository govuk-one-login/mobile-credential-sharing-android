package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class VerifyCredentialTest {
    @get:Rule
    val composeTestRule = VerifyCredentialRule(createComposeRule())

    private val bluetoothStateMonitor = FakeBluetoothStateMonitor()
    private val logger = SystemLogger()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun `bluetooth system prompt is displayed when state is bluetooth disabled`() = runTest {
        composeTestRule.setContent {
            VerifyCredential(
                viewModel = VerifyCredentialViewModel(
                    logger, bluetoothStateMonitor,
                )
            )
        }

        bluetoothStateMonitor.emit(BluetoothStatus.OFF)
        composeTestRule.waitForIdle()

        composeTestRule.assertBluetoothPromptIsDisplayed()
    }

    @Test
    fun `scanner is displayed when prerequisites are met`() = runTest {
        composeTestRule.setContent {
            VerifyCredential(
                viewModel = VerifyCredentialViewModel(
                    logger, bluetoothStateMonitor,
                )
            )
        }

        bluetoothStateMonitor.emit(BluetoothStatus.ON)
        composeTestRule.waitForIdle()

        composeTestRule.assertScannerIsDisplayed()
    }
}