package uk.gov.onelogin.sharing.verifier.scan

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriOne
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModelAssertions.isInInitialState
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModelHelper.monitor
import uk.gov.onelogin.sharing.verifier.scan.state.CompleteVerifierScannerState

@RunWith(AndroidJUnit4::class)
class VerifierScannerViewModelTest {

    private var hasReset = false
    private val resettable = Resettable {
        hasReset = true
    }

    private val model by lazy {
        VerifierScannerViewModel(
            state = CompleteVerifierScannerState(),
            resettable = setOf(resettable)
        )
    }

    @Test
    fun initialState() = runTest {
        monitor(model)
        assertTrue(hasReset)

        assertThat(
            model,
            isInInitialState()
        )
    }

    @Test
    fun resettingModelAlsoClearsInjectedResettable() = runTest {
        monitor(model)

        model.update(exampleUriOne)
        model.update(true)

        hasReset = false
        model.reset()

        assertThat(
            model,
            isInInitialState()
        )
        assertTrue(hasReset)
    }
}
