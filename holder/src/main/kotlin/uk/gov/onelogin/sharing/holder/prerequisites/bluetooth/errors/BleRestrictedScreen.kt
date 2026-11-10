package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.errors

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.launch
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.componentsv2.images.GdsIcon
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreen
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreenIcon
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R

@Composable
internal fun BleRestrictedScreen(
    modifier: Modifier = Modifier,
    viewModel: BleRestrictedViewModel = metroViewModel(),
    onGoBackToDocuments: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("BleRestrictedScreen")
        viewModel.sendScreenEvent(
            context.getEnglishString(R.string.holder_ble_restricted_title)
        )
    }

    BackHandler(enabled = true) {
        scope.launch {
            viewModel.reset()
            onGoBackToDocuments()
        }
    }

    BleRestrictedContent(
        modifier = modifier,
        onGoBackToDocuments = {
            viewModel.sendGoBackEvent(
                context.getEnglishString(R.string.holder_ble_restricted_button)
            )
            viewModel.reset()
            onGoBackToDocuments()
        }
    )
}

@Suppress("LongMethod")
@Composable
internal fun BleRestrictedContent(
    modifier: Modifier = Modifier,
    onGoBackToDocuments: () -> Unit = {}
) {
    ErrorScreen(
        modifier = modifier.fillMaxWidth(),
        icon = { horizontalPadding ->
            GdsIcon(
                image = ImageVector.vectorResource(ErrorScreenIcon.ErrorIcon.icon),
                contentDescription = stringResource(ErrorScreenIcon.ErrorIcon.description),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        title = { horizontalPadding ->
            GdsHeading(
                text = stringResource(R.string.holder_ble_restricted_title),
                modifier = Modifier
                    .padding(horizontal = horizontalPadding),
                textAlign = GdsHeadingAlignment.CenterAligned
            )
        },
        supportingText = null,
        body = { horizontalPadding ->
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacingDouble),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                ) {
                    Text(
                        text = stringResource(R.string.holder_ble_restricted_body_1),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.holder_ble_restricted_body_2),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(R.string.holder_ble_restricted_body_3),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        primaryButton = {
            GdsButton(
                text = stringResource(R.string.holder_ble_restricted_button),
                buttonType = ButtonTypeV2.Primary(),
                onClick = onGoBackToDocuments,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@PreviewContent
@Composable
internal fun BleRestrictedContentPreview() {
    GdsTheme {
        Surface {
            BleRestrictedContent(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
