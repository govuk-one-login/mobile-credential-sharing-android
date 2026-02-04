package uk.gov.onelogin.sharing.verifier.scan

import android.Manifest
import android.content.Context
import android.content.res.Resources
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.di.VerifierGraph
import uk.gov.onelogin.sharing.verifier.di.createTestGraph

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class VerifierScannerGrantedTest {

    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources
    private val appGraph = createTestGraph()

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA
    )

    @get:Rule
    val composeTestRule = VerifierScannerRule(
        resources = resources,
        composeTestRule = createComposeRule(),
        appGraph = appGraph
    )

    @Test
    fun permissionGrantedTextIsShown() = runTest {
        composeTestRule.run {
            setContent {
                val graph = remember {
                    createGraphFactory<VerifierGraph.Factory>().create(
                        appGraph = appGraph
                    )
                }

                CompositionLocalProvider(
                    LocalMetroViewModelFactory provides graph.metroViewModelFactory
                ) {
                    VerifierScanner(
                        modifier = Modifier,
                        onInvalidBarcode = {},
                        onValidBarcode = {}
                    )
                }
            }
            assertCameraViewfinderIsDisplayed()
        }
    }

    @Test
    fun permissionGrantedTextRenderedWithPermissionState() = runTest {
        composeTestRule.run {
            setContent {
                val graph = remember {
                    createGraphFactory<VerifierGraph.Factory>().create(
                        appGraph = appGraph
                    )
                }

                CompositionLocalProvider(
                    LocalMetroViewModelFactory provides graph.metroViewModelFactory
                ) {
                    VerifierScanner(
                        modifier = Modifier,
                        permissionState = rememberPermissionState(
                            permission = Manifest.permission.CAMERA
                        ),
                        onInvalidBarcode = {},
                        onValidBarcode = {}
                    )
                }
            }

            assertCameraViewfinderIsDisplayed()
        }
    }
}
