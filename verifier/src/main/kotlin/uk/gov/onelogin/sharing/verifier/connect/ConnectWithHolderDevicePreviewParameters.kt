package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import uk.gov.onelogin.sharing.bluetooth.api.adapter.FakeBluetoothAdapterProvider

@OptIn(ExperimentalPermissionsApi::class)
internal data class ConnectWithHolderDevicePreviewParameters(
    override val values: Sequence<ConnectWithHolderDeviceState> =
        cborEngagements.flatMap { base64EncodedEngagement ->
            bluetoothAdapters.flatMap {
                permissionStatuses.map {
                    ConnectWithHolderDeviceState(
                        base64EncodedEngagement = base64EncodedEngagement
                    )
                }
            }
        }.asSequence()
) : PreviewParameterProvider<ConnectWithHolderDeviceState> {

    companion object {
        private const val INVALID_CBOR =
            "gg8EaqoiWCA8Qib6bCfaav" +
                "-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap" +
                "-fAgG_APUB9ApQERERESIiMzNERFVVVVVVVf____8="
        private const val VALID_CBOR =
            "vwBjMS4wAZ8B2BhYTL8BAiABIVggk7wmKUmR5q" +
                "-ozZGB1uPAKfi8upiiA8JC88Ilgg8EaqoiWCA8Qib6bCfaav" +
                "-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap" +
                "-fAgG_APUB9ApQERERESIiMzNERFVVVVVVVf____8="

        private val cborEngagements = listOf(
            INVALID_CBOR,
            VALID_CBOR
        )
        private val permissionStatuses = listOf(
            PermissionStatus.Denied(shouldShowRationale = false),
            PermissionStatus.Granted
        )

        private val bluetoothAdapters = listOf(
            FakeBluetoothAdapterProvider(
                isEnabled = true
            ),
            FakeBluetoothAdapterProvider(
                isEnabled = false
            )
        )
    }
}
