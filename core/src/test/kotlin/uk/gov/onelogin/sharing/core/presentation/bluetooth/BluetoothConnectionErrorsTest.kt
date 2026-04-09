package uk.gov.onelogin.sharing.core.presentation.bluetooth

import android.content.Context
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.core.R as coreR

@RunWith(RobolectricTestParameterInjector::class)
class BluetoothConnectionErrorsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    enum class ErrorTitleResData(
        val error: BluetoothSessionError,
        @param:StringRes val expectedRes: Int,
        val expectedString: String
    ) {
        CONFIGURATION_ERROR(
            error = BluetoothSessionError.BluetoothConfigurationError,
            expectedRes = coreR.string.bluetooth_connection_error_failed,
            expectedString = "Bluetooth connection failed"
        ),
        GENERIC_ERROR(
            error = BluetoothSessionError.GenericError,
            expectedRes = coreR.string.bluetooth_connection_error_generic,
            expectedString = "Generic error"
        ),
        CONNECTION_ERROR(
            error = BluetoothSessionError.BluetoothConnectionError,
            expectedRes = coreR.string.bluetooth_disconnected_unexpectedly,
            expectedString = "Bluetooth disconnected unexpectedly"
        ),
        DISABLED_ERROR(
            error = BluetoothSessionError.BluetoothDisabledError,
            expectedRes = coreR.string.bluetooth_turned_off_verifier,
            expectedString = "Bluetooth was turned off on verifier device during session"
        ),
        PERMISSIONS_ERROR(
            error = BluetoothSessionError.BluetoothPermissionsError,
            expectedRes = coreR.string.bluetooth_permissions_revoked,
            expectedString = "Bluetooth permissions were revoked during the session"
        )
    }

    @Test
    fun `errorTitleRes returns correct string resource for each error`(
        @TestParameter input: ErrorTitleResData
    ) = runTest {
        assertEquals(input.expectedRes, errorTitleRes(input.error))
    }

    @Test
    fun `errorTitle returns correct string for each error`(
        @TestParameter input: ErrorTitleResData
    ) = runTest {
        assertEquals(input.expectedString, errorTitle(context, input.error))
    }
}
