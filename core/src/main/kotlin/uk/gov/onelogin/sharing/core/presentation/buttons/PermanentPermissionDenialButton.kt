package uk.gov.onelogin.sharing.core.presentation.buttons

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme

@Composable
fun PermanentPermissionDenialButton(
    context: Context,
    titleText: String,
    buttonText: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = titleText,
            textAlign = TextAlign.Center
        )

        GdsButton(
            modifier = Modifier.testTag("permissionPermanentDenialButton"),
            text = buttonText,
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
internal fun PermanentPermissionDenialButtonPreview() {
    GdsTheme {
        Column(
            modifier = Modifier
                .background(GdsLocalColorScheme.current.rowBackground)
                .padding(16.dp)
        ) {
            PermanentPermissionDenialButton(
                context = LocalContext.current,
                modifier = Modifier.testTag("preview"),
                titleText = "The camera permission is permanently denied.",
                buttonText = "Open app permissions"
            )
        }
    }
}
