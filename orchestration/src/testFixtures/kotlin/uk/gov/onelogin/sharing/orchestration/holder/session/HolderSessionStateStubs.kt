package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorStubs

object HolderSessionStateStubs {
    val userCancellation: HolderSessionState = HolderSessionState.Complete.Cancelled
    val userJourneyFailure: HolderSessionState =
        HolderSessionState.Complete.Failed(SessionErrorStubs.dummySessionError)
    val preflightEmptyPermissions: HolderSessionState = HolderSessionState.Preflight(emptyList())
    val successStub: HolderSessionState = HolderSessionState.Complete.Success()

    val consentPresentationStub: ConsentPresentation = ConsentPresentation(
        documents = emptyList(),
        organizationName = "Example Organisation",
        privacyPolicyUrl = "https://gov.uk/"
    )

    val awaitingUserConsentStub: HolderSessionState = HolderSessionState.AwaitingUserConsent(
        request = deviceRequestStub,
        presentation = consentPresentationStub
    )
}
