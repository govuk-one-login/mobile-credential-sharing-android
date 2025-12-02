package uk.gov.onelogin.sharing.holder

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import uk.gov.android.ui.componentsv2.matchers.SemanticsMatchers.hasRole
import uk.gov.onelogin.sharing.bluetooth.api.FakeMdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionManager
import uk.gov.onelogin.sharing.holder.HolderWelcomeTexts.HOLDER_WELCOME_TEXT
import uk.gov.onelogin.sharing.holder.QrCodeGenerator.QR_CODE_CONTENT_DESC
import uk.gov.onelogin.sharing.holder.presentation.BluetoothState
import uk.gov.onelogin.sharing.holder.presentation.HolderScreenContent
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreen
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeUiState
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeViewModel
import uk.gov.onelogin.sharing.security.FakeSessionSecurity
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub
import uk.gov.onelogin.sharing.security.engagement.Engagement
import uk.gov.onelogin.sharing.security.engagement.FakeEngagementGenerator

class HolderWelcomeScreenRule(
    composeTestRule: ComposeContentTestRule,
    private val enablePermissionsText: String,
    private val openAppSettingsText: String,
    private val permissionDeniedText: String
) : ComposeContentTestRule by composeTestRule {

    constructor(
        composeTestRule: ComposeContentTestRule,
        resources: Resources = ApplicationProvider.getApplicationContext<Context>().resources
    ) : this(
        composeTestRule = composeTestRule,
        enablePermissionsText = resources.getString(R.string.enable_bluetooth_permission),
        openAppSettingsText = resources.getString(R.string.open_app_permissions),
        permissionDeniedText = resources.getString(
            R.string.bluetooth_permission_permanently_denied
        )
    )

    val mdocSessionManager: MdocSessionManager = FakeMdocSessionManager()
    val dummyPublicKey = SessionSecurityTestStub.generateValidKeyPair()
    private val fakeSessionSecurity = FakeSessionSecurity(
        publicKey = dummyPublicKey
    )
    private val fakeEngagementGenerator = FakeEngagementGenerator(
        data = "${Engagement.QR_CODE_SCHEME}TEST_QR"
    )

    val viewModel: HolderWelcomeViewModel by lazy {
        HolderWelcomeViewModel(
            sessionSecurity = fakeSessionSecurity,
            engagementGenerator = fakeEngagementGenerator,
            mdocSessionManagerFactory = {
                mdocSessionManager.apply {
                    isBluetoothEnabled()
                }
            },
            dispatcher = Dispatchers.Main
        )
    }

    fun assertWelcomeTextIsDisplayed() = onNodeWithText(HOLDER_WELCOME_TEXT).assertIsDisplayed()

    fun assertEnablePermissionsButtonTextIsDisplayed() =
        onEnablePermissionsButtonText().assertIsDisplayed()

    fun assertPermanentlyDeniedTextIsDisplayed() =
        onPermissionPermanentlyDeniedButton().assertIsDisplayed()

    fun assertOpenAppSettingsButton() = onOpenAppSettingsButton().assertIsDisplayed()

    fun onEnablePermissionsButtonText() = onNodeWithText(enablePermissionsText)
        .assertExists()

    fun onOpenAppSettingsButton() = onNodeWithText(openAppSettingsText)
        .assertExists()
        .assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.Role,
                Role.Button
            )
        )
        .assertHasClickAction()

    fun onPermissionPermanentlyDeniedButton() = onNodeWithText(permissionDeniedText)

    fun assertQrCodeIsDisplayed() = onNodeWithContentDescription(QR_CODE_CONTENT_DESC)
        .assertIsDisplayed()
        .assert(hasRole(Role.Image))

    fun render() {
        setContent {
            HolderScreenContent(
                contentState = HolderWelcomeUiState(
                    qrData = "fakestring",
                    hasBluetoothPermissions = true,
                    bluetoothStatus = BluetoothState.Enabled
                )
            )
        }
    }
}
