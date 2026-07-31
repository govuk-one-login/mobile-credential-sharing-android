package uk.gov.onelogin.sharing.orchestration.verificationrequest

import android.os.Parcel
import kotlinx.parcelize.Parceler

/**
 * [Parceler] implementation for converting between [GbAttribute] and [Parcel].
 *
 * The implementation only considers the [GbAttribute.value] property.
 */
object GbAttributeParceler : Parceler<GbAttribute> {
    override fun GbAttribute.write(parcel: Parcel, flags: Int) {
        parcel.writeString(value)
    }

    override fun create(parcel: Parcel): GbAttribute {
        val value = requireNotNull(parcel.readString()) {
            "Cannot read 'GbAttribute' from parcel!"
        }

        return when (value) {
            GbAttribute.WelshLicence.value -> GbAttribute.WelshLicence

            GbAttribute.Title.value -> GbAttribute.Title

            GbAttribute.ProvisionalDrivingPrivileges.value ->
                GbAttribute.ProvisionalDrivingPrivileges

            else -> throw IllegalArgumentException("Unknown GbAttribute value: $value")
        }
    }
}
