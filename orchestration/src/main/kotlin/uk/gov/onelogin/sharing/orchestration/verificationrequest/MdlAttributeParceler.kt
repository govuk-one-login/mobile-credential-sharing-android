package uk.gov.onelogin.sharing.orchestration.verificationrequest

import android.os.Parcel
import kotlinx.parcelize.Parceler
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.AgeOver
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.BirthDate
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.BirthPlace
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.Custom
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.DocumentNumber
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.DrivingPrivileges
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.ExpiryDate
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.FamilyName
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.GivenName
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.IssueDate
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.IssuingAuthority
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.IssuingCountry
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.Portrait
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.ResidentAddress
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.ResidentCity
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.ResidentPostalCode
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute.UnDistinguishingSign

/**
 * [Parceler] implementation for converting between [MdlAttribute] and [Parcel].
 *
 * The implementation only considers the [MdlAttribute.value] property. This means that additional
 * properties are ignored, such as:
 *
 * - [AgeOver.age]
 * - [Custom.attributeName]
 */
object MdlAttributeParceler : Parceler<MdlAttribute> {
    override fun MdlAttribute.write(parcel: Parcel, flags: Int) {
        parcel.writeString(value)
    }

    @Suppress("CyclomaticComplexMethod")
    override fun create(parcel: Parcel): MdlAttribute {
        val value = requireNotNull(parcel.readString()) {
            "Cannot read 'MdlAttribute' from parcel!"
        }

        return when (value) {
            FamilyName.value -> FamilyName

            GivenName.value -> GivenName

            BirthDate.value -> BirthDate

            IssueDate.value -> IssueDate

            ExpiryDate.value -> ExpiryDate

            IssuingCountry.value -> IssuingCountry

            IssuingAuthority.value -> IssuingAuthority

            DocumentNumber.value -> DocumentNumber

            Portrait.value -> Portrait

            BirthPlace.value -> BirthPlace

            DrivingPrivileges.value -> DrivingPrivileges

            UnDistinguishingSign.value -> UnDistinguishingSign

            ResidentAddress.value -> ResidentAddress

            ResidentPostalCode.value -> ResidentPostalCode

            ResidentCity.value -> ResidentCity

            else -> {
                if (value.startsWith("age_over_")) {
                    AgeOver(value.takeLast(2).toInt())
                } else {
                    Custom(value)
                }
            }
        }
    }
}
