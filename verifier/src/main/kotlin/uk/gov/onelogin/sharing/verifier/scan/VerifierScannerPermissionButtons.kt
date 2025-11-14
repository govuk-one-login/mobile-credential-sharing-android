package uk.gov.onelogin.sharing.verifier.scan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.onelogin.sharing.verifier.R

object VerifierScannerPermissionButtons {
    @Composable
    fun CameraRequirePermissionButton(
        modifier: Modifier = Modifier,
        launchPermission: () -> Unit = {}
    ) {
        Column(modifier = modifier) {
            GdsButton(
                modifier = Modifier.testTag("permissionRequiredButton"),
                text =
                stringResource(
                    R.string.verifier_scanner_require_camera_permission
                ),
                buttonType = ButtonTypeV2.Primary(),
                onClick = {
                    launchPermission()
                }
            )
        }
    }

    @Composable
    fun CameraPermissionRationaleButton(
        modifier: Modifier = Modifier,
        launchPermission: () -> Unit = {}
    ) {
        Column(modifier = modifier) {
            GdsButton(
                modifier = Modifier.testTag("permissionRationaleButton"),
                text =
                stringResource(
                    R.string.verifier_scanner_require_camera_rationale
                ),
                buttonType = ButtonTypeV2.Secondary(),
                onClick = {
                    launchPermission()
                }
            )
        }
    }

    @Composable
    @OptIn(ExperimentalPermissionsApi::class)
    fun PermanentCameraDenial(context: Context, modifier: Modifier = Modifier) {
        Column(modifier = modifier) {
            Text(
                text =
                stringResource(
                    R.string.verifier_scanner_camera_permission_permanently_denied
                ),
                textAlign = TextAlign.Center
            )

            GdsButton(
                modifier = Modifier.testTag("permissionRationaleButton"),
                text = stringResource(
                    R.string.verifier_scanner_require_open_permissions
                ),
                buttonType = ButtonTypeV2.Primary(),
                onClick = {
                    val intent =
                        Intent(
                            ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts(
                                "package",
                                context.packageName,
                                null
                            )
                        )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            )
        }
    }
}
