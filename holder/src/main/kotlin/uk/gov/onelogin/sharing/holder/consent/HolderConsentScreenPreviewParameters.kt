package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import uk.gov.onelogin.sharing.cryptoService.verifier.DocRequestBuilderImpl
import uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare.consentcard.ConsentAttributeResolver
import uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare.consentcard.ConsentCard
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute

internal class HolderConsentScreenPreviewParameters(
    private val inputs: List<Pair<String, HolderConsentUiState>> = listOf(
        "Loading" to HolderConsentUiState.Loading,
        "Confirm consent" to loadedPreviewState,
        "More about DVS" to loadedPreviewState.copy(
            shouldShowMoreAboutDvs = true
        )
    )
) : PreviewParameterProvider<HolderConsentUiState> {
    override val values: Sequence<HolderConsentUiState>
        get() = inputs
            .map { it.second }
            .asSequence()

    override fun getDisplayName(index: Int): String = inputs[index].first

    internal companion object {
        internal val loadedPreviewState = HolderConsentUiState.Loaded(
            card = ConsentCard.from(
                ConsentAttributeResolver.toDisplayAttributes(
                    listOf(
                        ConsentAttribute("family_name", false, DocRequestBuilderImpl.NAMESPACE_ISO),
                        ConsentAttribute(
                            "document_number",
                            false,
                            DocRequestBuilderImpl.NAMESPACE_ISO
                        ),
                        ConsentAttribute("age_over_21", true, DocRequestBuilderImpl.NAMESPACE_ISO)
                    )
                )
            ),
            privacyPolicyUrl = "https://verifier.example/privacy",
            normalisedOrganizationName = "Organisation Ltd.",
            organizationName = "Organisation Ltd"
        )
    }
}
