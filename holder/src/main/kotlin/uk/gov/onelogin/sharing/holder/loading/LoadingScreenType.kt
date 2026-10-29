package uk.gov.onelogin.sharing.holder.loading

import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.holder.R

/**
 * The variants of the shared [LoadingScreen].
 *
 * Both variants render an identical loading UI (spinner + centred label); only the label text and
 * the GA4 identity differ. Each variant maps to a distinct transitional
 * [uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState].
 *
 * @property labelRes the centred label shown beneath the spinner.
 * @property screenName the GA4 screenView name.
 * @property firebaseScreenId the GA4 screenView id.
 */
enum class LoadingScreenType(
    @param:StringRes val labelRes: Int,
    val screenName: String,
    val firebaseScreenId: String
) {
    /** Shown during `ProcessingEstablishment` (QR code -> this -> Agree to share). */
    GettingDetails(
        labelRes = R.string.holder_getting_details_label,
        screenName = "Getting list of details",
        // TODO: Confirm the firebaseScreenID for the ProcessingEstablishment screen.
        firebaseScreenId = "TODO-PROCESSING-ESTABLISHMENT-SCREEN-ID"
    ),

    /** Shown during `ProcessingResponse` (Agree to share -> this -> Details shared). */
    SharingDetails(
        labelRes = R.string.holder_sharing_details_label,
        screenName = "Sharing your details",
        firebaseScreenId = "aedea082-0575-4f4e-babf-b2221d74756f"
    )
}
