package uk.gov.onelogin.sharing.verifier.connect.error

import androidx.annotation.StringRes
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import uk.gov.onelogin.sharing.core.R as coreR
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.core.presentation.bluetooth.errorTitleRes
import uk.gov.onelogin.sharing.verifier.R

@RunWith(Parameterized::class)
class ConnectWithHolderDeviceRouteErrorMappingTest(
    private val data: ConnectWithHolderDeviceRouteErrorData
) {
    @Test
    fun `maps BluetoothConfigurationError to invalid configuration string`() {
        val title = errorTitleRes(
            data.errorType
        )

        assertEquals(
            data.errorRes,
            title
        )
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}:{0}")
        fun data() = ConnectWithHolderDeviceRouteErrorData.entries
    }
}

enum class ConnectWithHolderDeviceRouteErrorData(
    @param:StringRes val errorRes: Int,
    val errorType: BluetoothSessionError
) {
    BLUETOOTH_CONFIGURATION(
        errorRes = R.string.bluetooth_connection_error_failed,
        errorType = BluetoothSessionError.BluetoothConfigurationError
    ),
    BLUETOOTH_GENERIC_ERROR(
        errorRes = R.string.bluetooth_connection_error_generic,
        errorType = BluetoothSessionError.GenericError
    ),
    BLUETOOTH_CONNECTION_ERROR(
        errorRes = coreR.string.bluetooth_disconnected_unexpectedly,
        errorType = BluetoothSessionError.BluetoothConnectionError
    ),
    BLUETOOTH_OFF_ERROR(
        errorRes = R.string.bluetooth_turned_off_verifier,
        errorType = BluetoothSessionError.BluetoothDisabledError
    ),
    BLUETOOTH_PERMISSIONS_ERROR(
        errorRes = coreR.string.bluetooth_permissions_revoked,
        errorType = BluetoothSessionError.BluetoothPermissionsError
    )
}
