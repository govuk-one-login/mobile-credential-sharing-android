package uk.gov.onelogin.sharing.verifier.scan

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.verifier.VerifierNavigationEvents
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModelAssertions.isInInitialState
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModelHelper.monitor
import uk.gov.onelogin.sharing.verifier.scan.state.CompleteVerifierScannerState
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs

@RunWith(AndroidJUnit4::class)
class VerifierScannerViewModelTest {

    private val orchestrator = FakeOrchestrator()

    private val model by lazy {
        VerifierScannerViewModel(
            state = CompleteVerifierScannerState(),
            orchestrator = orchestrator
        )
    }

    @Test
    fun initialState() = runTest {
        monitor(model)

        assertThat(
            model,
            isInInitialState()
        )
    }

    @Test
    fun `when barcode is valid, emit NavigateToDiagnostic and process QR`() = runTest {
        val validData = BarcodeDataResultStubs.validBarcodeDataResult.data

        model.navigationEvents.test {
            model.update(validData)

            val event = awaitItem()

            assert(event is VerifierNavigationEvents.NavigateToDiagnostic)
            assertEquals(validData, (event as VerifierNavigationEvents.NavigateToDiagnostic).qrCode)

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `when barcode is invalid, emit NavigateToInvalidScreen`() = runTest {
        val invalidData = BarcodeDataResultStubs.invalidBarcodeDataResultOne

        model.navigationEvents.test {
            model.update(invalidData)

            val event = awaitItem()

            assert(event is VerifierNavigationEvents.NavigateToInvalidScreen)

            ensureAllEventsConsumed()
        }
    }
}
