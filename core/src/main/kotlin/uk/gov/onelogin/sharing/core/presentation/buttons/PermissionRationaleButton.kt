package uk.gov.onelogin.sharing.core.presentation.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme

@Composable
fun PermissionRationaleButton(
    text: String,
    modifier: Modifier = Modifier,
    launchPermission: () -> Unit = {}
) {
    Column(modifier = modifier) {
        GdsButton(
            modifier = Modifier.testTag("permissionRationaleButton"),
            text = text,
            buttonType = ButtonTypeV2.Secondary(),
            onClick = {
                launchPermission()
            }
        )
    }
}

@Composable
@Preview
internal fun PermissionRationaleButtonPreview() {
    GdsTheme {
        Column(
            modifier = Modifier
                .background(GdsLocalColorScheme.current.listBackground)
                .padding(16.dp)
        ) {
            PermissionRationaleButton(
                launchPermission = {},
                text = "asd",
                modifier = Modifier.testTag("preview")
            )
        }
    }
}
