package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.errors

import android.content.Context
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.android.ui.componentsv2.rules.ComposeContentTestRuleExtensions.onNodeWithRole
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.holder.TestViewModelFactory
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.errors.BleRestrictedNavigationExt.configureBleRestricted
import uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.errors.BleRestrictedNavigationExt.navigateToBleRestricted
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(AndroidJUnit4::class)
class BleRestrictedNavigationTest {

    @get:Rule(order = 0)
    val dispatcherRule = MainDispatcherRule()

    @get:Rule(order = 1)
    val composeTestRule = BleRestrictedScreenRule()

    private lateinit var controller: TestNavHostController

    private val orchestrator = FakeOrchestrator(
        initialHolderState = MutableStateFlow(
            HolderSessionState.Preflight(missingPrerequisites = emptyList())
        )
    )

    private val viewModelFactory = TestViewModelFactory(
        viewModelProviders = mapOf(
            BleRestrictedViewModel::class to {
                BleRestrictedViewModel(
                    analyticsLogger = FakeAnalyticsLogger(),
                    orchestrator = orchestrator,
                    dispatcher = dispatcherRule.testDispatcher
                )
            }
        )
    )

    @Test
    fun `Can navigate to BLE restricted screen`() = runTest {
        composeTestRule.run {
            setContent {
                controller = prepareController(LocalContext.current)
                Render()
            }

            onNodeWithRole(Role.Button).performClick()

            waitUntil {
                controller.currentBackStackEntry?.toRoute<BleRestrictedRoute>() != null
            }

            assertValidUi()
        }
    }

    private fun prepareController(context: Context): TestNavHostController =
        TestNavHostController(context).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
        }

    @Composable
    private fun Render() {
        CompositionLocalProvider(
            LocalMetroViewModelFactory provides viewModelFactory
        ) {
            NavHost(
                navController = controller,
                startDestination = "entry"
            ) {
                composable("entry") {
                    Button(
                        onClick = { controller.navigateToBleRestricted() }
                    ) {
                        Text("This is a unit test")
                    }
                }
                configureBleRestricted()
            }
        }
    }
}
