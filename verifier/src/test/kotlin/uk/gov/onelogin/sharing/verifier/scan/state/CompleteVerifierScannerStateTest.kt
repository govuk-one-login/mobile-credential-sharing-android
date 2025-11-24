package uk.gov.onelogin.sharing.verifier.scan.state

import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriOne
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStateAssertions.hasBarcodeData
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStateAssertions.hasNoBarcodeData
import uk.gov.onelogin.sharing.verifier.scan.state.permission.PreviouslyDeniedPermissionStateAssertions.hasPreviouslyDeniedPermission
import uk.gov.onelogin.sharing.verifier.scan.state.permission.PreviouslyDeniedPermissionStateAssertions.hasPreviouslyGrantedPermission

@RunWith(AndroidJUnit4::class)
class CompleteVerifierScannerStateTest {

    private val state: VerifierScannerState.Complete = CompleteVerifierScannerState()
    private val uri = exampleUriOne.toUri()

    @Test
    fun initialState() {
        assertThat(
            state,
            allOf(
                hasNoBarcodeData(),
                hasPreviouslyGrantedPermission()
            )
        )
    }

    @Test
    fun canUpdateBarcodeDataResult() = runTest {
        backgroundScope.launch {
            state.barcodeDataResult.collect {}
        }

        state.update(uri)

        assertThat(
            state,
            hasBarcodeData(uri)
        )
    }

    @Test
    fun canUpdatePreviouslyDeniedPermission() = runTest {
        backgroundScope.launch {
            state.hasPreviouslyDeniedPermission.collect {}
        }

        state.update(true)

        assertThat(
            state,
            hasPreviouslyDeniedPermission()
        )
    }
}
