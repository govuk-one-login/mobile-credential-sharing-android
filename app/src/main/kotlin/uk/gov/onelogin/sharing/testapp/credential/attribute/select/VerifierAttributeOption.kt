package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import uk.gov.onelogin.sharing.orchestration.verificationrequest.AttributeGroup
import uk.gov.onelogin.sharing.orchestration.verificationrequest.GbAttribute
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute

private const val AGE_18 = 18
private const val AGE_21 = 21
private const val AGE_23 = 23

enum class VerifierAttributeOption(val displayName: String, val attributeGroup: AttributeGroup) {
    PORTRAIT_AND_AGE_OVER_21(
        displayName = "Portrait and Age Over 21",
        attributeGroup = AttributeGroup(
            mapOf(
                MdlAttribute.Portrait to false,
                MdlAttribute.AgeOver(AGE_21) to false
            )
        )
    ),
    PORTRAIT_NAME_RETAIN_AND_AGE_OVER_18(
        displayName = "Portrait and Name (Retain) and Age Over 18",
        attributeGroup = AttributeGroup(
            mapOf(
                MdlAttribute.Portrait to true,
                MdlAttribute.GivenName to true,
                MdlAttribute.FamilyName to true,
                MdlAttribute.AgeOver(AGE_18) to false
            )
        )
    ),
    MISSING_PORTRAIT(
        displayName = "Name (Missing Portrait)",
        attributeGroup = AttributeGroup(
            mapOf(
                MdlAttribute.GivenName to false
            )
        )
    ),
    NAME_TITLE_RETAIN_AND_AGE_OVER_23(
        displayName = "Name + Title (Retain) and Age Over 23",
        attributeGroup = AttributeGroup(
            attributes = mapOf(
                MdlAttribute.GivenName to true,
                MdlAttribute.AgeOver(AGE_23) to false
            ),
            gbAttributes = mapOf(
                GbAttribute.Title to true
            )
        )
    )
}
