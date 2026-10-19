package uk.gov.onelogin.sharing.holder.awaitingresolution

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.componentsv2.images.GdsIcon
import uk.gov.android.ui.patterns.centrealignedscreen.CentreAlignedScreen
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.holder.R

@Composable
internal fun AwaitingVerifierResolutionScreen(
    modifier: Modifier = Modifier,
    viewModel: AwaitingVerifierResolutionViewModel = metroViewModel(),
    onExitJourney: () -> Unit = {}
) {
    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("AwaitingVerifierResolutionScreen")
    }

    AwaitingVerifierResolutionContent(
        modifier = modifier,
        onGoBackToDocuments = {
            viewModel.reset()
            onExitJourney()
        }
    )
}

@Composable
internal fun AwaitingVerifierResolutionContent(
    modifier: Modifier = Modifier,
    onGoBackToDocuments: () -> Unit = {}
) {
    CentreAlignedScreen(
        title = { padding ->
            GdsHeading(
                text = stringResource(R.string.details_shared_heading),
                textAlign = GdsHeadingAlignment.CenterAligned,
                modifier = Modifier.padding(horizontal = padding)
            )
        },
        modifier = modifier.fillMaxSize(),
        image = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = padding),
                contentAlignment = Alignment.Center
            ) {
                GdsIcon(
                    image = Icons.Filled.CheckCircleOutline,
                    contentDescription = "Success",
                    modifier = Modifier
                        .width(75.dp)
                        .height(75.dp)
                        .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp),
                    color = GdsLocalColorScheme.current.successIcon
                )
            }
        },
        body = { padding ->
            item {
                Text(
                    text = stringResource(R.string.details_shared_body_1),
                    textAlign = TextAlign.Center,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padding)
                )
            }
            item {
                Text(
                    text = stringResource(R.string.details_shared_body_2),
                    textAlign = TextAlign.Center,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padding)
                )
            }
        },
        primaryButton = {
            GdsButton(
                text = stringResource(R.string.details_shared_button_text),
                buttonType = ButtonTypeV2.Primary(),
                onClick = onGoBackToDocuments,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
@Preview(showBackground = true)
internal fun AwaitingVerifierResolutionScreenPreview() {
    AwaitingVerifierResolutionContent()
}
