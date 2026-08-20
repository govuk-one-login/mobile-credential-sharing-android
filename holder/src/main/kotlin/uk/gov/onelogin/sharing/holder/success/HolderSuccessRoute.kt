package uk.gov.onelogin.sharing.holder.success

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class HolderSuccessRoute(val immediatelyReset: Boolean = false)
