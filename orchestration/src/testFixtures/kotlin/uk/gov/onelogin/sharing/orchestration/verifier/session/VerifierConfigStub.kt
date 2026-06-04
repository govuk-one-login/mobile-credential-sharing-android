package uk.gov.onelogin.sharing.orchestration.verifier.session

import io.mockk.mockk
import uk.gov.onelogin.sharing.orchestration.verificationrequest.AttributeGroup
import uk.gov.onelogin.sharing.orchestration.verificationrequest.DocumentType
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig

private const val AGE_18 = 18
private const val AGE_21 = 21

object VerifierConfigStub {
    val verifierConfigStub = VerifierConfig(
        verificationRequest = VerificationRequest(
            documentType = "mdoc",
            attributeGroup = AttributeGroup(emptyMap())
        ),
        trustedRootCertificate = mockk()
    )

    val photoAndAgeOver21Config = VerifierConfig(
        verificationRequest = VerificationRequest.typed(
            documentType = DocumentType.Mdl,
            attributeGroup = AttributeGroup(
                mapOf(
                    MdlAttribute.Portrait to false,
                    MdlAttribute.AgeOver(AGE_21) to false
                )
            )
        ),
        trustedRootCertificate = mockk()
    )

    val nameRetainAndAgeOver18Config = VerifierConfig(
        verificationRequest = VerificationRequest.typed(
            documentType = DocumentType.Mdl,
            attributeGroup = AttributeGroup(
                mapOf(
                    MdlAttribute.GivenName to true,
                    MdlAttribute.FamilyName to true,
                    MdlAttribute.AgeOver(AGE_18) to false
                )
            )
        ),
        trustedRootCertificate = mockk()
    )
}
