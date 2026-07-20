package uk.gov.onelogin.sharing.holder.cancellation.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.theme.m3.Buttons
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder

@Composable
internal fun HolderCancellationDialogContents(
    modifier: Modifier = Modifier,
    viewModel: HolderCancellationDialogViewModel = metroViewModel(),
    onDismiss: () -> Unit = {},
) = HolderCancellationDialogContents(
    modifier = modifier,
    onCancelJourney = viewModel::cancelJourney,
    onDismiss = onDismiss,
)

@Composable
internal fun HolderCancellationDialogContents(
    onCancelJourney: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderCancellationDialog")
    }

    Column(
        modifier = modifier,
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
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )

            GdsButton(
                text = "No",
                buttonType = ButtonTypeV2.Secondary(),
                onClick = onCancelJourney,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
internal fun HolderCancellationDialogContentsPreview() {
    GdsTheme {
        HolderCancellationDialogContents(
            modifier = Modifier
                .dropShadow(
                    shape = RoundedCornerShape(spacingDouble),
                    shadow = Shadow(
                        radius = spacingSingle,
                        spread = 6.dp,
                        color = Color(0x40000000),
                        offset = DpOffset(x = 4.dp, 4.dp)
                    )
                )
                .clip(RoundedCornerShape(spacingDouble))
                .background(GdsLocalColorScheme.current.dialogBackground)
                .padding(spacingDouble),
            onCancelJourney = {},
            onDismiss = {}
        )
    }
}