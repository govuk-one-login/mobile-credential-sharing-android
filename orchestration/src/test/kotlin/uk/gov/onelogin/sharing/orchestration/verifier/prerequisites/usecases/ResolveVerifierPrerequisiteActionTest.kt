package uk.gov.onelogin.sharing.orchestration.verifier.prerequisites.usecases

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
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.CameraState
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@RunWith(AndroidJUnit4::class)
class ResolveVerifierPrerequisiteActionTest {

    private lateinit var initialState: VerifierSessionState

    private val launcher: ActivityResultLauncher<PrerequisiteAction> = mockk(relaxed = true)

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialVerifierState = MutableStateFlow(initialState)
        )
    }

    private val logger = SystemLogger()

    private val resolver by lazy {
        ResolveVerifierPrerequisiteAction(
            logger = logger,
            orchestrator = orchestrator
        )
    }

    @Test
    fun `Launches actions from Missing prerequisites`() = runTest {
        val missingPrerequisite = MissingPrerequisite.Bluetooth(
            BluetoothState.PermissionNotGranted
        )
        initialState = VerifierSessionState.Preflight(
            listOf(missingPrerequisite)
        )
        resolver.resolve(launcher)

        verify { launcher.launch(withArg { missingPrerequisite.getAction() }) }
        confirmVerified(launcher)
    }

    @Test
    fun `Launches occur based on the number of recoverable actions`() = runTest {
        val missingPrerequisites = listOf(
            MissingPrerequisite.Bluetooth(BluetoothState.PermissionNotGranted),
            MissingPrerequisite.Camera(CameraState.Restricted)
        )
        initialState = VerifierSessionState.Preflight(missingPrerequisites)
        resolver.resolve(launcher)

        verify(exactly = missingPrerequisites.size - 1) {
            launcher.launch(any())
        }
        confirmVerified(launcher)
    }

    @Test
    fun `Unrecoverable prerequisites cannot launch actions`() = runTest {
        initialState = VerifierSessionState.Preflight(
            listOf(
                MissingPrerequisite.Bluetooth(BluetoothState.Unsupported)
            )
        )

        resolver.resolve(launcher)

        verify { launcher wasNot Called }
        confirmVerified(launcher)
    }

    @Test
    fun `Non-preflight states cannot launch actions`() = runTest {
        initialState = VerifierSessionState.NotStarted

        resolver.resolve(launcher)

        verify { launcher wasNot Called }
        confirmVerified(launcher)
    }
}
