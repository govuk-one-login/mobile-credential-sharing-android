package uk.gov.onelogin.sharing.orchestration.verificationrequest

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class AttributeGroup(val attributes: Map<MdlAttribute, Boolean>) : Parcelable
