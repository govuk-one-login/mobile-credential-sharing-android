package uk.gov.onelogin.sharing.cameraService.scan

import app.cash.turbine.test
import gov.onelogin.sharing.cameraservice.data.BarcodeDataResultStubs
import gov.onelogin.sharing.cameraservice.scan.FakeScanController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.cameraService.state.CompleteScannerState

class ScannerViewModelTest {

    private val model by lazy {
        ScannerViewModel(
            state = CompleteScannerState(),
            observer = FakeScanController()
        )
    }

    @Test
    fun `when state updates to Invalid, observer is notified and state is reset`() = runTest {
        val invalidData = BarcodeDataResultStubs.invalidBarcodeDataResultOne

        val invalidResult = BarcodeDataResult.Invalid(invalidData.data)

        model.barcodeDataResult.test {
            assertEquals(BarcodeDataResult.NotFound, awaitItem())
            model.update(invalidResult)

            val secondItem = awaitItem()

            assertEquals(invalidData.data, (secondItem as BarcodeDataResult.Invalid).data)
            assertEquals(BarcodeDataResult.NotFound, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun `when state updates to valid, observer is notified and state is reset`() = runTest {
        val validData = BarcodeDataResultStubs.validBarcodeDataResult.data
        val validResult = BarcodeDataResult.Valid(validData)

        model.barcodeDataResult.test {
            assertEquals(BarcodeDataResult.NotFound, awaitItem())
            model.update(validResult)

            val secondItem = awaitItem()

            assertEquals(validData, (secondItem as BarcodeDataResult.Valid).data)
            assertEquals(BarcodeDataResult.NotFound, awaitItem())
            ensureAllEventsConsumed()
        }
    }
}
