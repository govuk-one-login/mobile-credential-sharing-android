package uk.gov.onelogin.sharing.orchestration

import kotlin.test.Test
import kotlin.test.assertEquals
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

class ScanControllerImplTest {

    private val fakeOrchestrator = FakeOrchestrator()
    private val scanController = ScanControllerImpl(fakeOrchestrator)

    @Test
    fun `onScanResult delegates to orchestrator processQrCode`() {
        val result = BarcodeDataResult.Valid("mdoc:test")

        scanController.onScanResult(result)

        assertEquals(
            VerifierSessionState.Connecting,
            fakeOrchestrator.initialVerifierState.value
        )
    }

    @Test
    fun `reset delegates to orchestrator cancel`() {
        scanController.reset()

        assertEquals(1, fakeOrchestrator.cancelCount)
    }
}
