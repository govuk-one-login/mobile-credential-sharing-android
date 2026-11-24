package uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.patterns.leftalignedscreen.v3.LeftAlignedScreen
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.core.presentation.theme.SharingTheme
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.consent.HolderConsentScreenPreviewParameters
import uk.gov.onelogin.sharing.holder.consent.HolderConsentUiState
import uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare.consentcard.AgreeToShareCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReviewWhatYouWillShareContent(
    state: HolderConsentUiState.Loaded,
    modifier: Modifier = Modifier,
    onScreenLoad: (String) -> Unit = {},
    onReadPrivacyNotice: () -> Unit = {},
    onAgree: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val titleRes = R.string.holder_consent_screen_title
    val latestOnScreenLoad by rememberUpdatedState(onScreenLoad)

    val context = LocalContext.current
    val metrics = rememberMetricsStateHolder()
    val attributeCount = state.card?.attributeCount ?: 0

    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderConsentScreen")
        latestOnScreenLoad(context.getEnglishString(titleRes))
    }

    LeftAlignedScreen(
        modifier = modifier,
        title = { horizontalPadding ->
            GdsHeading(
                text = stringResource(titleRes),
                modifier = Modifier.padding(horizontal = horizontalPadding),
                textAlign = GdsHeadingAlignment.LeftAligned
            )
        },
        body = { horizontalPadding ->
            ConsentBody(state, horizontalPadding, onReadPrivacyNotice)
        },
        primaryButton = {
            GdsButton(
                text = pluralStringResource(
                    R.plurals.consent_screen_agree_button,
                    attributeCount,
                    attributeCount
                ),
                buttonType = ButtonTypeV2.Primary(),
                onClick = onAgree,
                enabled = !state.isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirmConsent")
            )
        },
        secondaryButton = {
            GdsButton(
                text = stringResource(R.string.holder_consent_screen_cancel_button),
                buttonType = ButtonTypeV2.Secondary(),
                onClick = onCancel,
                enabled = !state.isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("denyConsent")
            )
        }
    )
}

@Composable
private fun ColumnScope.ConsentBody(
    state: HolderConsentUiState.Loaded,
    horizontalPadding: Dp,
    onReadPrivacyNotice: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(spacingSingle)
    ) {
        Text(
            text = stringResource(
                R.string.holder_consent_organization_sentence,
                state.normalisedOrganizationName
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        GdsButton(
            text = stringResource(
                R.string.holder_consent_screen_privacy_notice_link,
                state.organizationName
            ),
            buttonType = ButtonTypeV2.Secondary(),
            onClick = onReadPrivacyNotice,
            textAlign = TextAlign.Start,
            contentPosition = Arrangement.Start,
            contentModifier = Modifier.fillMaxWidth()
        )
    }
    state.card?.let { card ->
        Text(
            text = stringResource(R.string.holder_consent_screen_what_you_share_heading),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .semantics { heading() }
        )
        SharingTheme {
            Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                AgreeToShareCard(
                    card = card,
                    organizationName = state.organizationName
                )
            }
        }
    }
}

@PreviewContent
@Composable
internal fun ReviewWhatYouWillShareContentPreview() {
    GdsTheme {
        Surface {
            ReviewWhatYouWillShareContent(
                modifier = Modifier.fillMaxSize(),
                state = HolderConsentScreenPreviewParameters.loadedPreviewState
            )
        }
    }
}
