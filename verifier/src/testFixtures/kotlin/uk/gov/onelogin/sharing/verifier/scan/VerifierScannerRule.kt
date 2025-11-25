package uk.gov.onelogin.sharing.verifier.scan

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasFlags
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import org.hamcrest.CoreMatchers.allOf
import uk.gov.android.ui.componentsv2.matchers.SemanticsMatchers.hasRole
import uk.gov.onelogin.sharing.verifier.R
import uk.gov.onelogin.sharing.verifier.scan.BarcodeAnalysisUrlContractAssertions.hasState

/**
 * JUnit 4 Rule for encapsulating assertion / performance behaviour for the [VerifierScanner] UI
 * composable.
 */
class VerifierScannerRule(
    composeTestRule: ComposeContentTestRule,
    private val openAppSettingsText: String,
    private val permissionDeniedText: String,
    private val permissionGrantedText: String
) : ComposeContentTestRule by composeTestRule {

    /**
     * Convenience constructor that extracts [String] values via the provided [resources] parameter.
     */
    constructor(
        composeTestRule: ComposeContentTestRule,
        resources: Resources = ApplicationProvider.getApplicationContext<Context>().resources
    ) : this(
        composeTestRule = composeTestRule,
        openAppSettingsText = resources.getString(R.string.open_app_permissions),
        permissionDeniedText = resources.getString(R.string.enable_camera_permission_to_continue),
        permissionGrantedText = resources.getString(R.string.camera_permission_is_enabled)
    )

    fun assertCameraViewfinderIsDisplayed() = onCameraViewfinder().assertIsDisplayed()

    /**
     * Requires the use of [Intents.init] and [Intents.release] as part of test classes / functions
     * that utilise this function.
     */
    fun assertIntentLaunched(expected: Uri) {
        intended(hasState(expected))
    }

    fun assertOpenAppSettingsButtonIsDisplayed() = onOpenAppSettingsButton().assertIsDisplayed()

    fun assertPermissionDeniedButtonIsDisplayed() = onPermissionDeniedButton().assertIsDisplayed()

    fun assertPermissionGrantedTextIsDisplayed() = onPermissionGrantedText().assertIsDisplayed()

    fun onCameraViewfinder() = onNodeWithTag("cameraViewfinder").assertExists()

    fun onOpenAppSettingsButton() = onNodeWithText(openAppSettingsText)
        .assertExists()
        .assert(hasRole(Role.Button))
        .assertHasClickAction()

    fun onPermissionDeniedButton() = onNodeWithText(permissionDeniedText)
        .assertExists()
        .assert(hasRole(Role.Button))
        .assertHasClickAction()

    fun onPermissionGrantedText() = onNodeWithText(permissionGrantedText)
        .assertExists()

    fun performOpenAppSettingsClick() = onOpenAppSettingsButton().performClick().also {
        intended(
            allOf(
                hasAction("android.settings.APPLICATION_DETAILS_SETTINGS"),
                hasData("package:uk.gov.onelogin.sharing.verifier.test"),
                hasFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        )
    }

    fun performPermissionDeniedClick() = onPermissionDeniedButton().performClick()

    @OptIn(ExperimentalPermissionsApi::class)
    fun render(
        modifier: Modifier = Modifier,
        onInvalidBarcode: (String) -> Unit = {},
        onValidBarcode: (String) -> Unit = {}
    ) {
        setContent {
            VerifierScanner(
                modifier = modifier,
                onInvalidBarcode = onInvalidBarcode,
                onValidBarcode = onValidBarcode
            )
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    fun render(
        model: VerifierScannerViewModel,
        modifier: Modifier = Modifier,
        onInvalidBarcode: (String) -> Unit = {},
        onValidBarcode: (String) -> Unit = {}
    ) {
        setContent {
            VerifierScanner(
                modifier = modifier,
                viewModel = model,
                onInvalidBarcode = onInvalidBarcode,
                onValidBarcode = onValidBarcode
            )
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    fun render(
        permissionState: @Composable () -> PermissionState,
        modifier: Modifier = Modifier,
        onInvalidBarcode: (String) -> Unit = {},
        onValidBarcode: (String) -> Unit = {}
    ) {
        setContent {
            VerifierScanner(
                modifier = modifier,
                permissionState = permissionState(),
                onInvalidBarcode = onInvalidBarcode,
                onValidBarcode = onValidBarcode
            )
        }
    }
}
