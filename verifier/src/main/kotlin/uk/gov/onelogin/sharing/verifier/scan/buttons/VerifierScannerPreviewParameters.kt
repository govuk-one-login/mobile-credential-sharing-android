package uk.gov.onelogin.sharing.verifier.scan.buttons

import android.Manifest
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus

@OptIn(ExperimentalPermissionsApi::class)
class VerifierScannerPreviewParameters(
    requestedPermission: String = Manifest.permission.CAMERA,
    override val values: Sequence<Pair<PermissionState, Boolean>> = listOf(
        PermissionStatus.Denied(shouldShowRationale = false),
        PermissionStatus.Denied(shouldShowRationale = true),
        PermissionStatus.Granted
    ).map {
        object : PermissionState {
            override val permission: String get() = requestedPermission
            override val status: PermissionStatus get() = it
            override fun launchPermissionRequest() {
                // do nothing as this is for previews
            }
        }
    }.flatMap { state ->
        listOf(true, false).map { state to it }
    }.asSequence()
) : PreviewParameterProvider<Pair<PermissionState, Boolean>>
