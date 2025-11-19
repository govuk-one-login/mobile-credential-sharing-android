package uk.gov.onelogin.sharing.verifier.scan

import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModelAssertions.hasPreviouslyDeniedPermission
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModelAssertions.hasUri
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModelAssertions.isInInitialState
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModelHelper.monitor

@RunWith(AndroidJUnit4::class)
class VerifierScannerViewModelTest {

    private val model = VerifierScannerViewModel()
    private val uri = "https://this.is.a.unit.test".toUri()

    @Test
    fun initialState() = runTest {
        monitor(model)

        assertThat(
            model,
            isInInitialState()
        )
    }

    @Test
    fun previouslyDeniedPermissionIsUpdatable() = runTest {
        monitor(model)

        model.update(true).join()

        assertThat(
            model,
            hasPreviouslyDeniedPermission()
        )
    }

    @Test
    fun uriIsUpdatable() = runTest {
        monitor(model)

        model.update(uri).join()

        assertThat(
            model,
            hasUri(uri)
        )
    }

    @Test
    fun modelIsResettable() = runTest {
        monitor(model)

        model.update(uri).join()
        model.update(true).join()

        model.reset()

        assertThat(
            model,
            isInInitialState()
        )
    }
}
