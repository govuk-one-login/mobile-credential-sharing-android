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
    GettingDetails(
        labelRes = R.string.holder_getting_details_label,
        screenName = "Getting list of details",
        firebaseScreenId = "06328966-cd42-49c4-a573-9140ace45297"
    ),

    SharingDetails(
        labelRes = R.string.holder_sharing_details_label,
        screenName = "Sharing your details",
        firebaseScreenId = "aedea082-0575-4f4e-babf-b2221d74756f"
    )
}
