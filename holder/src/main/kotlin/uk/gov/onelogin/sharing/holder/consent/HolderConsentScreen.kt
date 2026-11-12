package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import uk.gov.android.ui.componentsv2.progress.GdsProgressIndicator
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.cryptoService.verifier.DocRequestBuilderImpl.Companion.NAMESPACE_ISO
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsPreviewParameters
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.MoreAboutDvsViewModel
import uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare.ReviewWhatYouWillShareContent
import uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare.consentcard.ConsentAttributeResolver
import uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare.consentcard.ConsentCard
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentation

@Composable
internal fun HolderConsentScreen(
    modifier: Modifier = Modifier,
    viewModel: HolderConsentViewModel = metroViewModel()
) {
    val uiState: HolderConsentUiState by viewModel.uiState.collectAsStateWithLifecycle()

    HolderConsentContent(
        modifier = modifier,
        state = uiState,
        onAgree = viewModel::onAccept,
        onLoadReviewWhatYouWillShare = viewModel::sendReviewWhatYouWillShareScreenEvent,
        onReadPrivacyNotice = viewModel::onReadPrivacyNotice,
        onCancel = viewModel::onCancelAndGoBack
    )
}

@Composable
internal fun HolderConsentContent(
    state: HolderConsentUiState,
    modifier: Modifier = Modifier,
    onLoadReviewWhatYouWillShare: (String) -> Unit = {},
    onAgree: () -> Unit = {},
    onReadPrivacyNotice: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    when (state) {
        HolderConsentUiState.Loading -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GdsProgressIndicator()
            }
        }

        is HolderConsentUiState.ConfirmConsent -> {
            ReviewWhatYouWillShareContent(
                modifier = modifier,
                state = state,
                onReadPrivacyNotice = onReadPrivacyNotice,
                onScreenLoad = onLoadReviewWhatYouWillShare,
                onAgree = onAgree,
                onCancel = onCancel
            )
        }

        is HolderConsentUiState.MoreAboutDvs -> {
            // do nothing for now
        }
    }
}
@PreviewContent
@Composable
internal fun HolderConsentScreenPreview(
    @PreviewParameter(provider = HolderConsentScreenPreviewParameters::class)
    state: HolderConsentUiState
) {
    GdsTheme {
        Surface {
            HolderConsentContent(
                modifier = Modifier.fillMaxSize(),
                state = state,
            )
        }
    }
}

internal class HolderConsentScreenPreviewParameters(
    private val inputs: List<HolderConsentUiState> = listOf(
        HolderConsentUiState.Loading,
        confirmConsentPreviewState,
        HolderConsentUiState.MoreAboutDvs(
            normalisedOrganizationName = "Organisation Ltd.",
            privacyPolicyUrl = "https://verifier.example/privacy"
        )
    )
) : PreviewParameterProvider<HolderConsentUiState> {
    override val values: Sequence<HolderConsentUiState>
        get() = inputs.asSequence()

    override fun getDisplayName(index: Int): String? = inputs[index]::class.java.simpleName

    internal companion object {
        internal val confirmConsentPreviewState = HolderConsentUiState.ConfirmConsent(
            card = ConsentCard.from(
                ConsentAttributeResolver.toDisplayAttributes(
                    listOf(
                        ConsentAttribute("family_name", false, NAMESPACE_ISO),
                        ConsentAttribute("document_number", false, NAMESPACE_ISO),
                        ConsentAttribute("age_over_21", true, NAMESPACE_ISO)
                    )
                )
            ),
            privacyPolicyUrl = "https://verifier.example/privacy",
            normalisedOrganizationName = "Organisation Ltd.",
            organizationName = "Organisation Ltd"
        )
    }
}
