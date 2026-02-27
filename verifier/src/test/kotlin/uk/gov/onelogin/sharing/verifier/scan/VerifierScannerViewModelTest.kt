package uk.gov.onelogin.sharing.verifier.scan

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModelAssertions.isInInitialState
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModelHelper.monitor
import uk.gov.onelogin.sharing.verifier.scan.state.CompleteVerifierScannerState

@RunWith(AndroidJUnit4::class)
class VerifierScannerViewModelTest {

    private val model by lazy {
        VerifierScannerViewModel(
            state = CompleteVerifierScannerState()
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
}
