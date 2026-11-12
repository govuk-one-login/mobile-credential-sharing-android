package uk.gov.onelogin.sharing.holder.consent

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreen
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.core.presentation.theme.SharingTheme
import uk.gov.onelogin.sharing.cryptoService.verifier.DocRequestBuilderImpl.Companion.NAMESPACE_ISO
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute

@Composable
internal fun HolderConsentScreen(viewModel: HolderConsentViewModel = metroViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val titleRes = R.string.holder_consent_screen_title

    val metrics = rememberMetricsStateHolder()
    LaunchedEffect(Unit) {
        metrics.putScreenState("HolderConsentScreen")

        viewModel.sendScreenEvent(context.getEnglishString(titleRes))
    }

    uiState?.let {
        HolderConsentContent(
            state = it,
            titleRes = titleRes,
            onAgree = viewModel::onAccept,
            onReadPrivacyNotice = viewModel::onReadPrivacyNotice,
            onCancel = viewModel::onCancelAndGoBack
        )
    } ?: CircularProgressIndicator()
}

@Composable
internal fun HolderConsentContent(
    state: HolderConsentUiState,
    @StringRes titleRes: Int,
    onAgree: () -> Unit = {},
    onReadPrivacyNotice: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val attributeCount = state.card?.attributeCount ?: 0

    LeftAlignedScreen(
        modifier = Modifier.fillMaxSize(),
        title = { horizontalPadding ->
            GdsHeading(
                text = stringResource(titleRes),
                modifier = Modifier.padding(horizontal = horizontalPadding),
                textAlign = GdsHeadingAlignment.LeftAligned
            )
        },
        body = { horizontalPadding ->
            consentBody(state, horizontalPadding, onReadPrivacyNotice)
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
                modifier = Modifier.fillMaxWidth()
            )
        },
        secondaryButton = {
            GdsButton(
                text = stringResource(R.string.holder_consent_screen_cancel_button),
                buttonType = ButtonTypeV2.Secondary(),
                onClick = onCancel,
                enabled = !state.isBusy,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

private fun LazyListScope.consentBody(
    state: HolderConsentUiState,
    horizontalPadding: Dp,
    onReadPrivacyNotice: () -> Unit
) {
    item {
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
    }
    state.card?.let { card ->
        item {
            Text(
                text = stringResource(R.string.holder_consent_screen_what_you_share_heading),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
            )
        }
        item {
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
}

@PreviewContent
@Composable
internal fun HolderConsentScreenPreview() {
    val card = ConsentCard.from(
        ConsentAttributeResolver.toDisplayAttributes(
            listOf(
                ConsentAttribute("family_name", false, NAMESPACE_ISO),
                ConsentAttribute("document_number", false, NAMESPACE_ISO),
                ConsentAttribute("age_over_21", true, NAMESPACE_ISO)
            )
        )
    )
    GdsTheme {
        Surface {
            HolderConsentContent(
                state = HolderConsentUiState(
                    card = card,
                    privacyPolicyUrl = "https://verifier.example/privacy",
                    normalisedOrganizationName = "Organisation Ltd.",
                    organizationName = "Organisation Ltd"
                ),
                titleRes = R.string.holder_consent_screen_title
            )
        }
    }
}
