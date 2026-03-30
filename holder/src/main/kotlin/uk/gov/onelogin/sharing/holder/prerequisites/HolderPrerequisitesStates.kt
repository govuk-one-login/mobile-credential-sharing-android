package uk.gov.onelogin.sharing.holder.prerequisites

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

internal class HolderPrerequisitesStates : PreviewParameterProvider<HolderSessionState> {
    private val data = listOf(
        "On launching screen" to HolderSessionState.NotStarted,
        "Missing requirements" to HolderSessionState.Preflight(listOf()),
        "Generating QR code" to HolderSessionState.ReadyToPresent,
        "Navigating to welcome screen" to HolderSessionState.PresentingEngagement(
            "composable preview"
        )
    )

    override val values: Sequence<HolderSessionState> = data
        .map { it.second }
        .asSequence()

    override fun getDisplayName(index: Int): String? = data
        .map { it.first }
        .getOrNull(index)
}
