package uk.gov.onelogin.sharing.verifier.scan

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModelAssertions.hasPreviouslyDeniedPermission
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModelAssertions.hasPreviouslyGrantedPermission
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModelHelper.monitor

@RunWith(AndroidJUnit4::class)
class VerifierScannerViewModelTest {

    private val viewModel = VerifierScannerViewModel()

    @Test
    fun previouslyDeniedPermissionIsUpdatable() = runTest {
        monitor(viewModel)

        assertThat(
            viewModel,
            hasPreviouslyGrantedPermission()
        )

        var job = viewModel.update(true)

        while (!job.isCompleted) {
            // infinite loop until it's finished
        }

        assertThat(
            viewModel,
            hasPreviouslyDeniedPermission()
        )

        job = viewModel.reset()

        while (!job.isCompleted) {
            // infinite loop until it's finished
        }

        assertThat(
            viewModel,
            hasPreviouslyGrantedPermission()
        )
    }
}
