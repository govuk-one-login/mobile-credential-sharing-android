package uk.gov.onelogin.sharing.holder.consent

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import com.google.testing.junit.testparameterinjector.TestParameter
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.componentsv2.rules.ComposeContentTestRuleExtensions.onNodeWithRole
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub
import uk.gov.onelogin.sharing.holder.StubMetroViewModelFactory
import uk.gov.onelogin.sharing.holder.consent.AgreeToShareCardSamples.ORGANIZATION_NAME
import uk.gov.onelogin.sharing.holder.consent.HolderConsentNavigationExt.configureHolderConsentScreen
import uk.gov.onelogin.sharing.holder.consent.HolderConsentNavigationExt.navigateToHolderConsentScreen
import uk.gov.onelogin.sharing.holder.navigation.TestNavHostControllerHelper.prepareController
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentDocument
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentNamespace
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentationTestData.createConsentPresentation
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

@RunWith(RobolectricTestParameterInjector::class)
class HolderConsentNavigationExtTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = HolderConsentScreenRule()

    private val holderState = MutableStateFlow<HolderSessionState>(
        HolderSessionState.NotStarted
    )

    private var confirmConsentCount = 0

    private val orchestrator = FakeOrchestrator(
        initialHolderState = holderState,
        onConfirmConsent = { confirmConsentCount++ }
    )

    private val viewModel by lazy {
        HolderConsentViewModel(
            orchestrator = orchestrator,
            analyticsLogger = FakeAnalyticsLogger(),
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    private val viewModelFactory: MetroViewModelFactory = StubMetroViewModelFactory(
        viewModelProviders = mapOf(
            HolderConsentViewModel::class to { viewModel }
        )
    )

    private val presentationWithoutRetain = consentPresentation(
        mapOf(
            "family_name" to false,
            "document_number" to false,
            "driving_privileges" to false,
            "issue_date" to false,
            "expiry_date" to false,
            "portrait" to false
        )
    )

    private lateinit var navController: TestNavHostController

    private fun consentPresentation(elements: Map<String, Boolean>) = createConsentPresentation(
        organizationName = ORGANIZATION_NAME,
        privacyPolicyUrl = "https://verifier.example/privacy",
        documents = listOf(
            ConsentDocument(
                docType = "org.iso.18013.5.1.mDL",
                namespaces = listOf(
                    ConsentNamespace(
                        nameSpace = "org.iso.18013.5.1",
                        attributes = elements.map { (id, retain) ->
                            ConsentAttribute(
                                elementIdentifier = id,
                                intentToRetain = retain,
                                nameSpace = "org.iso.18013.5.1"
                            )
                        }
                    )
                )
            )
        )
    )

    @Test
    fun `Back button behaviour is handled at a higher level`() =
        runTest(dispatcherRule.testDispatcher) {
            holderState.update {
                HolderSessionState.AwaitingUserConsent(
                    request = DeviceRequestStub.deviceRequestStub,
                    presentation = presentationWithoutRetain
                )
            }

            composeTestRule.run {
                setContent {
                    navController = prepareController(LocalContext.current)

                    Render()
                }

                onNodeWithRole(Role.Button).performClick()
                waitForIdle()

                runOnUiThread {
                    navController.popBackStack()
                }
                waitForIdle()

                assertThat(
                    navController.currentDestination?.route,
                    equalTo("previous")
                )
            }
        }

    @Composable
    private fun Render() {
        CompositionLocalProvider(
            LocalMetroViewModelFactory provides viewModelFactory
        ) {
            GdsTheme {
                NavHost(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    startDestination = "previous"
                ) {
                    composable("previous") {
                        Button(
                            onClick = {
                                navController.navigateToHolderConsentScreen()
                            }
                        ) {
                            Text("Navigate")
                        }
                    }
                    configureHolderConsentScreen()
                }
            }
            }
        }
    }
