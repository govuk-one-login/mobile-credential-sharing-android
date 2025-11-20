package uk.gov.onelogin.sharing.verifier.scan.buttons

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation
import uk.gov.onelogin.sharing.verifier.R

@Composable
@OptIn(ExperimentalPermissionsApi::class)
@RequiresImplementation(
    details = [
        ImplementationDetail(
            ticket = "DCMAW-16275",
            description = "Finalise UI for permanent camera permission denial"
        )
    ]
)
fun PermanentCameraDenial(context: Context, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text =
            stringResource(
                R.string.verifier_scanner_camera_permission_permanently_denied
            ),
            textAlign = TextAlign.Companion.Center
        )

        GdsButton(
            modifier = Modifier.testTag("permissionPermanentDenialButton"),
            text = stringResource(
                R.string.verifier_scanner_require_open_permissions
            ),
            buttonType = ButtonTypeV2.Primary(),
            onClick = {
                val intent =
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
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

@Composable
@Preview
internal fun PermanentCameraDenialPreview() {
    GdsTheme {
        Column(
            modifier = Modifier
                .background(GdsLocalColorScheme.current.listBackground)
                .padding(16.dp)
        ) {
            PermanentCameraDenial(
                context = LocalContext.current,
                modifier = Modifier.testTag("preview")
            )
        }
    }
}
