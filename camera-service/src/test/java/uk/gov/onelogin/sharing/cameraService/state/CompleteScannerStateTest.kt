package uk.gov.onelogin.sharing.cameraService.state

import androidx.test.ext.junit.runners.AndroidJUnit4
import gov.onelogin.sharing.cameraservice.data.BarcodeDataResultStateAssertions.hasBarcodeData
import gov.onelogin.sharing.cameraservice.data.BarcodeDataResultStateAssertions.hasNoBarcodeData
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriOne

@RunWith(AndroidJUnit4::class)
class CompleteScannerStateTest {

    private val state: ScannerState.Complete = CompleteScannerState()

    @Test
    fun initialState() {
        assertThat(
            state,
            allOf(
                hasNoBarcodeData()
            )
        )
    }

    @Test
    fun canUpdateBarcodeDataResult() = runTest {
        backgroundScope.launch {
            state.barcodeDataResult.collect {}
        }

        state.update(exampleUriOne)

        assertThat(
            state,
            hasBarcodeData(exampleUriOne)
        )
    }
}
