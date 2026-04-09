package uk.gov.onelogin.sharing.verifier.connect.error

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.core.R as coreR
import uk.gov.onelogin.sharing.verifier.R

/**
 * Test data used with [StringsXmlTest] for resource IDs that defer to another string resource.
 *
 * Ensures that tests would fail when deferred strings update without updating this test data.
 */
enum class BluetoothConnectionErrorDeferredStringsXmlData(
    @param:StringRes val defersTo: Int,
    @param:StringRes val resourceId: Int
) {
    BLUETOOTH_CONNECTION_ERROR_GENERIC(
        defersTo = coreR.string.generic_error,
        resourceId = R.string.bluetooth_connection_error_generic
    ),
    BLUETOOTH_CONNECTION_ERROR_INVALID_CONFIGURATION(
        defersTo = coreR.string.bluetooth_connection_failed,
        resourceId = R.string.bluetooth_connection_error_failed
    ),
    BLUETOOTH_CONNECTION_ERROR_TRY_AGAIN(
        defersTo = coreR.string.try_again,
        resourceId = R.string.bluetooth_connection_error_try_again
    )
}
