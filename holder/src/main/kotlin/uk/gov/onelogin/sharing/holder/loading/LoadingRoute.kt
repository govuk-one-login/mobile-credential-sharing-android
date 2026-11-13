package uk.gov.onelogin.sharing.holder.loading

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

/**
 * Navigation route for the shared [LoadingScreen].
 *
 * @param type The UI variant (label + GA4 identity) derived from the progress phase.
 */
@Keep
@Serializable
internal data class LoadingRoute(val type: LoadingScreenType)
