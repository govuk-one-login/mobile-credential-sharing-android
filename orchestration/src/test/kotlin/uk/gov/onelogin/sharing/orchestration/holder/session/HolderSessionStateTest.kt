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
            Preflight(listOf()),
            AwaitingUserConsent(mockk(relaxed = true)),
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
}
