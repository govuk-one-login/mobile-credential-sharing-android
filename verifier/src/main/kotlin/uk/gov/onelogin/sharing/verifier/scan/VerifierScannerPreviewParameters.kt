package uk.gov.onelogin.sharing.verifier.scan

import android.Manifest
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState

/**
 * [PreviewParameterProvider] implementation for use within [VerifierScannerContentPreview].
 *
 * Creates the cartesian product of all expected [PermissionState] states, along with both boolean
 * values.
 */
@OptIn(ExperimentalPermissionsApi::class)
class VerifierScannerPreviewParameters(
    override val values: Sequence<Pair<MultiplePermissionsState, Boolean>> = listOf(
        PermissionStatus.Denied(shouldShowRationale = false),
        PermissionStatus.Denied(shouldShowRationale = true),
        PermissionStatus.Granted
    ).map {
        FakeMultiplePermissionsState(
            permissions = listOf(
                FakePermissionState(
                    permission = Manifest.permission.CAMERA,
                    status = it
                )
            ),
            onLaunchPermission = {}
        )
    }.flatMap { state ->
        listOf(true, false).map { state to it }
    }.asSequence()
) : PreviewParameterProvider<Pair<MultiplePermissionsState, Boolean>>
