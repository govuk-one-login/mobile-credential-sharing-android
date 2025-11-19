package uk.gov.onelogin.sharing.verifier.scan

import androidx.core.net.toUri
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

    private val uri = "https://this.is.a.unit.test".toUri()

    @Test
    fun initialState() = runTest {
        val model = VerifierScannerViewModel()
        monitor(model)

        assertThat(
            model,
            isInInitialState()
        )
    }

    @Test
    fun modelIsResettable() = runTest {
        val model = VerifierScannerViewModel(
            state = CompleteVerifierScannerState()
        )

        monitor(model)

        model.update(uri)
        model.update(true)

        model.reset()

        assertThat(
            model,
            isInInitialState()
        )
    }
}
