package uk.gov.onelogin.sharing.verifier.scan

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class VerifierScannerPreviewParametersTest {

    private val parameters = VerifierScannerPreviewParameters().values.toList()

    @Test
    fun verifyNumberOfParameters() = runTest {
        assertEquals(
            EXPECTED_SIZE,
            parameters.size
        )
    }

    @Test
    fun thereAreThreeUniquePermissionRequestStates() = runTest {
        val uniqueStates = parameters.map(Pair<PermissionState, Boolean>::first).distinct()
        assertEquals(
            EXPECTED_STATES,
            uniqueStates.size
        )
    }

    companion object {
        private const val EXPECTED_SIZE = 6
        private const val EXPECTED_STATES = 3
    }
}
