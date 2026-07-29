package uk.gov.onelogin.sharing.orchestration.verificationrequest

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@TypeParceler<GbAttribute, GbAttributeParceler>()
sealed class GbAttribute(val value: String) : Parcelable {
    abstract fun validate(data: Any): Boolean

    @Serializable
    @TypeParceler<GbAttribute, GbAttributeParceler>()
    data object WelshLicence : GbAttribute("welsh_licence") {
        override fun validate(data: Any) = data is Boolean
    }

    @Serializable
    @TypeParceler<GbAttribute, GbAttributeParceler>()
    data object Title : GbAttribute("title") {
        override fun validate(data: Any) = data is String && data.length <= MdlAttribute.MAX_LENGTH
    }

    @Serializable
    @TypeParceler<GbAttribute, GbAttributeParceler>()
    data object ProvisionalDrivingPrivileges : GbAttribute("provisional_driving_privileges") {
        override fun validate(data: Any) = data is List<*>
    }

    companion object {
        const val NAMESPACE = "org.iso.18013.5.1.GB"
    }
}
