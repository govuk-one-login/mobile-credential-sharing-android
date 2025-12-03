package uk.gov.onelogin.sharing.verifier.connect

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.undecodeableBarcodeDataResult
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.validBarcodeDataResult

/**
 * Property bag data class for holding state for the [ConnectWithHolderDeviceRule]'s rendering
 * functions.
 */
@OptIn(ExperimentalPermissionsApi::class)
data class ConnectWithHolderDeviceState(val base64EncodedEngagement: String) {
    companion object {
        /**
         * State for when the [ConnectWithHolderDeviceState.base64EncodedEngagement] cannot be
         * decoded for bluetooth connection purposes.
         */
        val undecodeableState = ConnectWithHolderDeviceState(
            base64EncodedEngagement = undecodeableBarcodeDataResult.data
        )

        /**
         * State that includes a valid [ConnectWithHolderDeviceState.base64EncodedEngagement] for
         * bluetooth connection purposes.
         */
        val decodeableState = ConnectWithHolderDeviceState(
            base64EncodedEngagement = validBarcodeDataResult.data
        )
    }
}
