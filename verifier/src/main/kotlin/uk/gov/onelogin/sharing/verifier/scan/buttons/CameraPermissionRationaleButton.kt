package uk.gov.onelogin.sharing.verifier.scan.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.verifier.R

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
@Preview
internal fun CameraPermissionRationaleButtonPreview() {
    GdsTheme {
        Column(
            modifier = Modifier
                .background(GdsLocalColorScheme.current.listBackground)
                .padding(16.dp)
        ) {
            CameraPermissionRationaleButton(
                launchPermission = {},
                modifier = Modifier.testTag("preview")
            )
        }
    }
}
