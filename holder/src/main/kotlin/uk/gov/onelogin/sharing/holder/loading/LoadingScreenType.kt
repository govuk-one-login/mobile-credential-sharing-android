package uk.gov.onelogin.sharing.holder.loading

import androidx.annotation.Keep
import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.ProgressPresentation

/**
 * The UI mapping of a [ProgressPresentation.Phase] for the shared [LoadingScreen].
 *
 * Both variants render an identical loading UI (spinner + centred label); only the label text and
 * the GA4 identity differ. The variant is derived from the [ProgressPresentation.Phase] carried on
 * the transitional [uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState]. The
 * cancellation and toolbar close-button rules are driven separately by the
 * [uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState] itself, not by this
 * type.
 *
 * @property labelRes the centred label shown beneath the spinner.
 * @property screenName the GA4 screenView name.
 * @property firebaseScreenId the GA4 screenView id.
 */
@Keep
enum class LoadingScreenType(
    @param:StringRes val labelRes: Int,
    val screenName: String,
    val firebaseScreenId: String
) {
    GettingDetails(
        labelRes = R.string.holder_getting_details_label,
        screenName = "Getting list of details",
        firebaseScreenId = "06328966-cd42-49c4-a573-9140ace45297"
    ),

    SharingDetails(
        labelRes = R.string.holder_sharing_details_label,
        screenName = "Sharing your details",
        firebaseScreenId = "aedea082-0575-4f4e-babf-b2221d74756f"
    );

    companion object {
        /**
         * Maps an orchestration-layer [ProgressPresentation.Phase] to its UI [LoadingScreenType].
         */
        fun from(phase: ProgressPresentation.Phase): LoadingScreenType = when (phase) {
            ProgressPresentation.Phase.GettingDetails -> GettingDetails
            ProgressPresentation.Phase.SharingDetails -> SharingDetails
        }
    }
}
