package uk.gov.onelogin.sharing.orchestration.verificationrequest

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@TypeParceler<MdlAttribute, MdlAttributeParceler>()
sealed class MdlAttribute(open val value: String) : Parcelable {
    abstract fun validate(data: Any): Boolean

    override fun describeContents(): Int = 0

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object FamilyName : MdlAttribute("family_name") {
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object GivenName : MdlAttribute("given_name") {
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object BirthDate : MdlAttribute("birth_date") {
        override fun validate(data: Any) = data is String && FULL_DATE_PATTERN.matches(data)
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object IssueDate : MdlAttribute("issue_date") {
        override fun validate(data: Any) = data is String && FULL_DATE_PATTERN.matches(data)
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object ExpiryDate : MdlAttribute("expiry_date") {
        override fun validate(data: Any) = data is String && FULL_DATE_PATTERN.matches(data)
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object IssuingCountry : MdlAttribute("issuing_country") {
        override fun validate(data: Any) = data is String && data.length == COUNTRY_CODE_LENGTH
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object IssuingAuthority : MdlAttribute("issuing_authority") {
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object DocumentNumber : MdlAttribute("document_number") {
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object Portrait : MdlAttribute("portrait") {
        override fun validate(data: Any) = true
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object BirthPlace : MdlAttribute("birth_place") {
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object DrivingPrivileges : MdlAttribute("driving_privileges") {
        override fun validate(data: Any) = data is List<*>
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object UnDistinguishingSign : MdlAttribute("un_distinguishing_sign") {
        override fun validate(data: Any) = data is String && data.isNotEmpty()
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object ResidentAddress : MdlAttribute("resident_address") {
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object ResidentPostalCode : MdlAttribute("resident_postal_code") {
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data object ResidentCity : MdlAttribute("resident_city") {
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data class AgeOver(private val age: Int) : MdlAttribute("age_over_%02d".format(age)) {
        init {
            require(age in MIN_AGE..MAX_AGE) {
                "age must be between $MIN_AGE and $MAX_AGE, was $age"
            }
        }

        override fun validate(data: Any) = data is Boolean
    }

    @Serializable
    @TypeParceler<MdlAttribute, MdlAttributeParceler>()
    data class Custom(val attributeName: String) : MdlAttribute(attributeName) {
        override fun validate(data: Any) = true
    }

    companion object {
        private val FULL_DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}".toRegex()
        private const val COUNTRY_CODE_LENGTH = 2
        private const val MIN_AGE = 0
        private const val MAX_AGE = 99
        const val MAX_LENGTH = 150
    }
}
