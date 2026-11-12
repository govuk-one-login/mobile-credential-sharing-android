package uk.gov.onelogin.sharing.holder.qrcode

import android.content.Context
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.holder.TestViewModelFactory
import uk.gov.onelogin.sharing.holder.qrcode.QrCodeNavigationExt.configureQrCodeScreen
import uk.gov.onelogin.sharing.holder.qrcode.QrCodeNavigationExt.navigateToQrCodeScreen
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs

@RunWith(AndroidJUnit4::class)
class QrCodeNavigationExtTest {

    @get:Rule
    val composeTestRule = QrCodeScreenRule(createComposeRule())

    private lateinit var controller: TestNavHostController
    private val viewModel by lazy {
        QrCodeViewModel(
            orchestrator = FakeOrchestrator(
                initialHolderState = MutableStateFlow(
                    HolderSessionStateStubs.presentingEngagementStub
                )
            ),
            analyticsLogger = StoredAnalyticsLogger()
        )
    }
    private val viewModelFactory: MetroViewModelFactory = TestViewModelFactory(
        viewModelProviders = mapOf(QrCodeViewModel::class to { viewModel })
    )

    @Test
    @UiThreadTest
    fun `Can navigate to the QR code screen`() = runTest {
        composeTestRule.run {
            setContent {
                controller = prepareController(LocalContext.current)
                Render()
            }
            onNodeWithText(ENTRY_BUTTON).performClick()
            waitUntil {
                controller.currentBackStackEntry?.destination
                    ?.hasRoute<QrCodeRoute>() ?: false
            }
            assertHeadingIsDisplayed()
        }
    }

    @Test
    @UiThreadTest
    fun `Back press navigates to the previous screen`() = runTest {
        composeTestRule.run {
            setContent {
                controller = prepareController(LocalContext.current)
                Render()
            }
            onNodeWithText(ENTRY_BUTTON).performClick()
            waitUntil {
                controller.currentBackStackEntry?.destination
                    ?.hasRoute<QrCodeRoute>() ?: false
            }

            runOnUiThread { controller.popBackStack() }
            waitForIdle()

            waitUntil { controller.currentBackStackEntry?.destination?.route == ENTRY_ROUTE }
        }
    }

    private fun prepareController(context: Context): TestNavHostController =
        TestNavHostController(context).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
        }

    @Composable
    private fun Render() {
        CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
            NavHost(
                navController = controller,
                startDestination = ENTRY_ROUTE
            ) {
                composable(ENTRY_ROUTE) {
                    Button(onClick = { controller.navigateToQrCodeScreen() }) {
                        Text(ENTRY_BUTTON)
                    }
                }
                configureQrCodeScreen()
            }
        }
    }

    private companion object {
        const val ENTRY_BUTTON = "Navigate"
        const val ENTRY_ROUTE = "entry"
    }
}
