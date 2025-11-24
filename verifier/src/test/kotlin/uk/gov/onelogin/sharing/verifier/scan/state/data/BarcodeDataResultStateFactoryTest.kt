package uk.gov.onelogin.sharing.verifier.scan.state.data

import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriOne
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStateAssertions.hasBarcodeData
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStateAssertions.hasNoBarcodeData

@RunWith(AndroidJUnit4::class)
class BarcodeDataResultStateFactoryTest {

    private val flow = MutableStateFlow<BarcodeDataResult>(BarcodeDataResult.NotFound)
    private val state: BarcodeDataResultState.Complete = BarcodeDataResultState.Complete.from(flow)
    private val uri = exampleUriOne.toUri()

    @Test
    fun initialState() {
        assertThat(
            state,
            hasNoBarcodeData()
        )
    }

    @Test
    fun canUpdateBarcodeDataResult() = runTest {
        backgroundScope.launch {
            flow.collect {}
        }

        state.update(uri)

        assertThat(
            state,
            hasBarcodeData(uri)
        )
    }
}
