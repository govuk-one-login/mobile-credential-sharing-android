package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.adapter.FakeBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.api.scanner.FakeAndroidBluetoothScanner
import uk.gov.onelogin.sharing.security.DecoderStub.VALID_CBOR
import uk.gov.onelogin.sharing.security.DecoderStub.validDeviceEngagementDto
import uk.gov.onelogin.sharing.security.DeviceEngagementStub.ENGAGEMENT_EXPECTED_BASE_64
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.decodableDeniedState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.decodableGrantedState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.fakePermissionStateDenied
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.fakePermissionStateGranted
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.undecodableState
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.validWithCorrectBluetoothSetup
import uk.gov.onelogin.sharing.verifier.session.FakeVerifierSession

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(RobolectricTestParameterInjector::class)
class ConnectWithHolderDeviceScreenTest {

    @get:Rule
    val composeTestRule = ConnectWithHolderDeviceRule(createComposeRule())

    @TestParameter(valuesProvider = ConnectWithHolderDeviceRenderProvider::class)
    lateinit var renderFunction: (
        ConnectWithHolderDeviceRule,
        ConnectWithHolderDeviceState,
        Modifier,
        SessionEstablishmentViewModel,
        MultiplePermissionsState
    ) -> Unit

    val mdocVerifierSession = FakeVerifierSession()

    @Test
    fun cannotDecodeProvidedCborString() = runTest {
        val fakeBluetoothProvider = FakeBluetoothAdapterProvider(isEnabled = false)
        val fakeBluetoothScanner = FakeAndroidBluetoothScanner()

        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = fakeBluetoothProvider,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger(),
            verifierSessionFactory = { mdocVerifierSession }
        )

        composeTestRule.run {
            renderFunction(
                this,
                undecodableState,
                Modifier,
                testViewModel,
                fakePermissionStateDenied
            )
            assertBasicInformationIsDisplayed()
            assertErrorIsDisplayed()
            assertDeviceEngagementDataDoesNotExist()
            assertBluetoothPermissionIsDenied()
            assertDeviceBluetoothIsDisabled()
            assertIsNotSearchingForBluetoothDevices()
        }
    }

    @Test
    fun validCborExistsOnScreen() = runTest {
        composeTestRule.run {
            val testViewModel = SessionEstablishmentViewModel(
                bluetoothAdapterProvider = FakeBluetoothAdapterProvider(isEnabled = false),
                scanner = FakeAndroidBluetoothScanner(),
                logger = SystemLogger(),
                verifierSessionFactory = { mdocVerifierSession }
            )
            renderFunction(
                this,
                decodableDeniedState,
                Modifier,
                testViewModel,
                fakePermissionStateDenied
            )
            assertBasicInformationIsDisplayed()
            assertErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
            assertBluetoothPermissionIsDenied()
            assertDeviceBluetoothIsDisabled()
            assertIsNotSearchingForBluetoothDevices()
        }
    }

    @Test
    fun bluetoothPermissionIsGrantedButDeviceBluetoothIsDisabled() = runTest {
        val fakeBluetoothProvider = FakeBluetoothAdapterProvider(isEnabled = false)
        val fakeBluetoothScanner = FakeAndroidBluetoothScanner()

        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = fakeBluetoothProvider,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger(),
            verifierSessionFactory = { mdocVerifierSession }
        )

        val stateForTest = decodableGrantedState

        composeTestRule.waitForIdle()

        composeTestRule.run {
            renderFunction(this, stateForTest, Modifier, testViewModel, fakePermissionStateGranted)
            assertBasicInformationIsDisplayed()
            assertErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
            assertBluetoothPermissionIsGranted()
            assertDeviceBluetoothIsDisabled()
            assertIsNotSearchingForBluetoothDevices()
        }
    }

    @Test
    fun bluetoothPermissionIsGranted() = runTest {
        val fakeBluetoothProvider = FakeBluetoothAdapterProvider(isEnabled = false)
        val fakeBluetoothScanner = FakeAndroidBluetoothScanner()

        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = fakeBluetoothProvider,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger(),
            verifierSessionFactory = { mdocVerifierSession }
        )
        composeTestRule.run {
            renderFunction(
                this,
                decodableGrantedState,
                Modifier,
                testViewModel,
                fakePermissionStateGranted
            )
            assertBasicInformationIsDisplayed()
            assertErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
            assertBluetoothPermissionIsGranted()
            assertDeviceBluetoothIsDisabled()
            assertIsNotSearchingForBluetoothDevices()
        }
    }

    @Test
    fun grantedAndEnabledBluetoothWithValidCborStartsScanning() = runTest {
        val fakeBluetoothProvider = FakeBluetoothAdapterProvider(isEnabled = true)
        val fakeBluetoothScanner = FakeAndroidBluetoothScanner()

        val testViewModel = SessionEstablishmentViewModel(
            bluetoothAdapterProvider = fakeBluetoothProvider,
            scanner = fakeBluetoothScanner,
            logger = SystemLogger(),
            verifierSessionFactory = { mdocVerifierSession }
        )

        composeTestRule.run {
            renderFunction(
                this,
                validWithCorrectBluetoothSetup,
                Modifier,
                testViewModel,
                fakePermissionStateGranted
            )
            assertBasicInformationIsDisplayed()
            assertErrorDoesNotExist()
            assertDeviceEngagementDataIsDisplayed()
            assertBluetoothPermissionIsGranted()
            assertDeviceBluetoothIsEnabled()
            assertIsSearchingForBluetoothDevices()
        }
    }

    @Test
    fun shouldShowErrorScreenWhenShowErrorScreenSetTrue() {
        val errorState = ConnectWithHolderDeviceState(
            showErrorScreen = true,
            hasAllPermissions = true,
            isBluetoothEnabled = true
        )

        composeTestRule.setContent {
            ConnectWithHolderDeviceScreenContent(
                base64EncodedEngagement = ENGAGEMENT_EXPECTED_BASE_64,
                contentState = errorState,
                engagementData = validDeviceEngagementDto,
                permissionsGranted = true,
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithText("Generic error").isDisplayed()
    }

    @Test
    fun connectWithHolderDevicePreviewRendersWithValidCbor() {
        composeTestRule.setContent {
            ConnectWithHolderDevicePreview(
                base64EncodedEngagement = VALID_CBOR
            )
        }
        composeTestRule.onNodeWithText("Successfully scanned QR code data:").assertIsDisplayed()
    }
}
