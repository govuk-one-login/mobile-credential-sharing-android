package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import uk.gov.onelogin.sharing.cryptoService.verifier.DocRequestBuilderImpl
import uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare.consentcard.ConsentAttributeResolver
import uk.gov.onelogin.sharing.holder.consent.reviewwhatyouwillshare.consentcard.ConsentCard
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute

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