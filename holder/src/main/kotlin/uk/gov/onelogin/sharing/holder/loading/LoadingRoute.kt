package uk.gov.onelogin.sharing.holder.loading

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class LoadingRoute(val type: LoadingScreenType)
