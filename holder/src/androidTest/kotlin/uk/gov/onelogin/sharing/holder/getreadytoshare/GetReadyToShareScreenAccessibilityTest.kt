package uk.gov.onelogin.sharing.holder.getreadytoshare

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.accessibility.AccessibilityHelper.performAccessibilityCheck
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases.ResolveHolderPrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.api.usecases.RetryPrerequisitesNavigator
import uk.gov.onelogin.sharing.prerequisites.api.usecases.RetryPrerequisitesNavigator.NavigationEvent
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites
import uk.gov.onelogin.sharing.prerequisites.permissions.FakePermissionChecker
import uk.gov.onelogin.sharing.prerequisites.usecases.RetryPrerequisitesNavigatorExt.from

@RunWith(AndroidJUnit4::class)
class GetReadyToShareScreenAccessibilityTest {
    @get:Rule
    val accessibilityRule = createAndroidComposeRule<ComponentActivity>()

    private var navigatorEvents = mutableListOf<NavigationEvent>()
    private var missingPrerequisites = mutableListOf(
        MissingPrerequisites.Bluetooth(state = BluetoothState.PermissionNotGranted)
    )

    private val logger = SystemLogger()

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }
    private val resolver by lazy {
        ResolveHolderPrerequisiteAction(
            logger = logger,
            orchestrator = orchestrator,
            permissionChecker = FakePermissionChecker()
        )
    }
    private val initialHolderState: HolderSessionState by lazy {
        HolderSessionState.Preflight(
            missingPrerequisites = missingPrerequisites
        ) { }
    }

    private val navigator by lazy {
        RetryPrerequisitesNavigator.from<HolderSessionState>(
            navigatorEvents.asFlow()
        )
    }

    private val viewModel by lazy {
        GetReadyToShareViewModel(
            navigator = navigator,
            orchestrator = orchestrator,
            resolver = resolver,
        )
    }

    @Test
    fun passesAccessibilityChecks() {
        accessibilityRule.performAccessibilityCheck { Render() }
    }

    @Composable
    private fun Render() {
        GdsTheme {
            GetReadyToShareScreen(
                viewModel = viewModel
            )
        }
    }
}