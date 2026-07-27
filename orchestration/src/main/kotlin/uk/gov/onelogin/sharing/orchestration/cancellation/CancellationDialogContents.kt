package uk.gov.onelogin.sharing.orchestration.cancellation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.android.ui.theme.spacingSingle

@Composable
fun CancellationDialogContents(
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(spacingDouble),
        border = BorderStroke(1.dp, Color.Gray),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(spacingDouble),
            verticalArrangement = Arrangement.spacedBy(spacingDouble),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Are you sure you want to cancel?")

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacingSingle)
            ) {
                GdsButton(
                    text = "Yes",
                    buttonType = ButtonTypeV2.Destructive(),
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                )

                GdsButton(
                    text = "No",
                    buttonType = ButtonTypeV2.Secondary(),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
internal fun CancellationDialogContentsPreview() {
    GdsTheme {
        CancellationDialogContents(
            modifier = Modifier
                .clip(RoundedCornerShape(spacingDouble))
                .background(GdsLocalColorScheme.current.dialogBackground)
                .padding(spacingDouble),
            onCancel = {},
            onDismiss = {}
        )
    }
}
