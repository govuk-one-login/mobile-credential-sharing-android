package uk.gov.onelogin.sharing.orchestration.holder.session

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.AwaitingUserConsent
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.AwaitingVerifierResolution
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.Cancelled
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.Failed
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.Success
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Preflight
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.PresentingEngagement
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.ProcessingEstablishment
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.ProcessingResponse
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.ReadyToPresent
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState

@RunWith(TestParameterInjector::class)
class HolderSessionStateTest {

    @Test
    fun `Certain states are cancellable by the User`(
        @TestParameter state: HolderSessionState = testValues(
            AwaitingUserConsent(mockk(relaxed = true), mockk(relaxed = true)),
            AwaitingVerifierResolution,
            ProcessingEstablishment,
            Preflight(listOf()),
            PresentingEngagement("unit test"),
            ProcessingResponse
        )
    ) {
        assertTrue {
            state.userCanCancel()
        }
    }

    @Test
    fun `Certain states cannot be cancelled by the User`(
        @TestParameter state: HolderSessionState = testValues(
            Cancelled,
            Failed(mockk(relaxed = true)),
            NotStarted,
            ReadyToPresent,
            Success(),
            HolderSessionState.SendingTermination
        )
    ) {
        assertFalse {
            state.userCanCancel()
        }
    }

    @Test
    fun `AwaitingUserConsent requires cancellation confirmation`() {
        assertTrue {
            AwaitingUserConsent(mockk(relaxed = true), mockk(relaxed = true))
                .shouldConfirmCancellation()
        }
    }

    @Test
    fun `AwaitingUserConsent is cancellable but hides the close button`() {
        val state = AwaitingUserConsent(mockk(relaxed = true), mockk(relaxed = true))

        assertTrue { state.userCanCancel() }
        assertTrue { state.hideCloseButton() }
    }

    @Test
    fun `Cancellable states other than AwaitingUserConsent do not hide the close button`(
        @TestParameter state: HolderSessionState = testValues(
            AwaitingVerifierResolution,
            ProcessingEstablishment,
            Preflight(listOf()),
            PresentingEngagement("unit test"),
            ProcessingResponse
        )
    ) {
        assertFalse {
            state.hideCloseButton()
        }
    }

    @Test
    fun `Preflight does not require cancellation confirmation`() {
        assertFalse {
            Preflight(listOf()).shouldConfirmCancellation()
        }
    }

    @Test
    fun `Preflight requires Bluetooth enabling when Bluetooth is powered off`() {
        assertTrue {
            HolderSessionStateStubs.preflightWithoutBluetooth.requiresBluetoothEnabled()
        }
    }

    @Test
    fun `Preflight does not require Bluetooth enabling for missing prerequisites`() {
        assertFalse { Preflight(listOf()).requiresBluetoothEnabled() }
    }

    @Test
    fun `Preflight does not require Bluetooth enabling when permission denied`() {
        assertFalse {
            HolderSessionStateStubs.preflightWithBluetooth(BluetoothState.PermissionNotGranted)
                .requiresBluetoothEnabled()
        }
    }

    @Test
    fun `Preflight does not require Bluetooth enabling when permission denied permanently`() {
        assertFalse {
            HolderSessionStateStubs.preflightWithBluetooth(
                BluetoothState.PermissionDeniedPermanently
            )
                .requiresBluetoothEnabled()
        }
    }

    @Test
    fun `Preflight requires Bluetooth permission when permission not granted`() {
        assertTrue {
            HolderSessionStateStubs.preflightBluetoothPermissionDenied.requiresBluetoothPermission()
        }
    }

    @Test
    fun `Preflight does not require Bluetooth permission for missing prerequisites`() {
        assertFalse { Preflight(listOf()).requiresBluetoothPermission() }
    }

    @Test
    fun `Preflight does not require Bluetooth permission when Bluetooth is powered off`() {
        assertFalse {
            HolderSessionStateStubs.preflightWithoutBluetooth.requiresBluetoothPermission()
        }
    }

    @Test
    fun `Preflight does not require Bluetooth permission when permission denied permanently`() {
        assertFalse {
            HolderSessionStateStubs.preflightWithBluetooth(
                BluetoothState.PermissionDeniedPermanently
            )
                .requiresBluetoothPermission()
        }
    }
}
