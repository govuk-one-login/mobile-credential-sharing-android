package uk.gov.onelogin.sharing.holder.loading

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

/**
 * Navigation route for the shared [LoadingScreen].
 *
 * @param type The UI variant (label + GA4 identity) derived from the progress phase.
 * @param cancellationAvailable Whether the User may cancel (Back / Close) during this phase. When
 * `false` the screen is a non-interactive transitional screen and intercepts system back.
 */
@Keep
@Serializable
internal data class LoadingRoute(
    val type: LoadingScreenType,
    val cancellationAvailable: Boolean
)
