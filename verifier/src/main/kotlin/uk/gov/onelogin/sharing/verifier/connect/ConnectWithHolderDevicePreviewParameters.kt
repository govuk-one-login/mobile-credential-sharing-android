package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState

@OptIn(ExperimentalPermissionsApi::class)
internal data class ConnectWithHolderDevicePreviewParameters(
    override val values: Sequence<ConnectWithHolderDeviceState> = sequenceOf(
        ConnectWithHolderDeviceState(
            // valid CBOR
            base64EncodedEngagement =
                "vwBjMS4wAZ8B2BhYTL8BAiABIVggk7wmKUmR5q" +
                    "-ozZGB1uPAKfi8upiiA8JC88Ilgg8EaqoiWCA8Qib6bCfaav" +
                    "-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap" +
                    "-fAgG_APUB9ApQERERESIiMzNERFVVVVVVVf____8=",
            permissionState = FakePermissionState.bluetoothConnect(
                status = PermissionStatus.Denied(shouldShowRationale = false)
            )
        ),
        ConnectWithHolderDeviceState(
            // invalid CBOR
            base64EncodedEngagement =
                "gg8EaqoiWCA8Qib6bCfaav" +
                    "-5A8QvfCEceATx1H9HR_Kj2ZnNeyxZLf__Ap" +
                    "-fAgG_APUB9ApQERERESIiMzNERFVVVVVVVf____8=",
            permissionState = FakePermissionState.bluetoothConnect(
                status = PermissionStatus.Denied(shouldShowRationale = false)
            )
        )
    )
) : PreviewParameterProvider<ConnectWithHolderDeviceState>
