package uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases

import androidx.activity.result.ActivityResultLauncher
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.Called
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.api.state.CameraState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites

@RunWith(AndroidJUnit4::class)
class ResolveHolderPrerequisiteActionTest {

    private lateinit var initialHolderState: HolderSessionState

    private val launcher: ActivityResultLauncher<PrerequisiteAction> = mockk(relaxed = true)

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val logger = SystemLogger()

    private val resolver by lazy {
        ResolveHolderPrerequisiteAction(
            logger = logger,
            orchestrator = orchestrator
        )
    }

    @Test
    fun `Launches actions from Missing prerequisites`() = runTest {
        val missingPrerequisite = MissingPrerequisites.Bluetooth(
            BluetoothState.PermissionNotGranted
        )
        initialHolderState = HolderSessionState.Preflight(
            listOf(missingPrerequisite)
        )
        resolver.resolve(launcher)

        verify { launcher.launch(withArg { missingPrerequisite.getAction() }) }
        confirmVerified(launcher)
    }

    @Test
    fun `Launches occur based on the number of recoverable actions`() = runTest {
        val missingPrerequisites = listOf(
            MissingPrerequisites.Bluetooth(BluetoothState.PermissionNotGranted),
            MissingPrerequisites.Camera(CameraState.Restricted)
        )
        initialHolderState = HolderSessionState.Preflight(missingPrerequisites)
        resolver.resolve(launcher)

        verify(exactly = missingPrerequisites.size - 1) {
            launcher.launch(any())
        }
        confirmVerified(launcher)
    }

    @Test
    fun `Unrecoverable prerequisites cannot launch actions`() = runTest {
        initialHolderState = HolderSessionState.Preflight(
            listOf(
                MissingPrerequisites.Bluetooth(BluetoothState.Unsupported)
            )
        )

        resolver.resolve(launcher)

        verify { launcher wasNot Called }
        confirmVerified(launcher)
    }

    @Test
    fun `Non-preflight states cannot launch actions`() = runTest {
        initialHolderState = HolderSessionState.NotStarted

        resolver.resolve(launcher)

        verify { launcher wasNot Called }
        confirmVerified(launcher)
    }
}
