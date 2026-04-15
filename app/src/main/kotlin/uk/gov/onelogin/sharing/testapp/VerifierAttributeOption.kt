package uk.gov.onelogin.sharing.testapp

import uk.gov.onelogin.sharing.orchestration.verificationrequest.AttributeGroup
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute

private const val AGE_18 = 18
private const val AGE_21 = 21

enum class VerifierAttributeOption(val displayName: String, val attributeGroup: AttributeGroup) {
    PHOTO_AND_AGE_OVER_21(
        displayName = "Photo and Age Over 21",
        attributeGroup = AttributeGroup(
            mapOf(
                MdlAttribute.Portrait to false,
                MdlAttribute.AgeOver(AGE_21) to false
            )
        )
    ),
    NAME_RETAIN_AND_AGE_OVER_18(
        displayName = "Name (Retain) and Age Over 18",
        attributeGroup = AttributeGroup(
            mapOf(
                MdlAttribute.GivenName to true,
                MdlAttribute.FamilyName to true,
                MdlAttribute.AgeOver(AGE_18) to false
            )
        )
    )
}
