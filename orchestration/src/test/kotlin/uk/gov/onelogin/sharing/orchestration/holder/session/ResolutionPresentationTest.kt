package uk.gov.onelogin.sharing.orchestration.holder.session

import kotlin.test.Test
import kotlin.test.assertEquals

class ResolutionPresentationTest {

    @Test
    fun `Approved resolves to DetailsShared`() = assertEquals(
        ResolutionPresentation(ResolutionResult.DetailsShared),
        ResolutionPresentation.from(HolderSessionState.Complete.SuccessReason.Approved)
    )

    @Test
    fun `UnfulfillableRequest resolves to UnfulfillableRequest`() = assertEquals(
        ResolutionPresentation(ResolutionResult.UnfulfillableRequest),
        ResolutionPresentation.from(HolderSessionState.Complete.SuccessReason.UnfulfillableRequest)
    )

    @Test
    fun `Denied resolves to a null result for immediate dismissal`() = assertEquals(
        ResolutionPresentation(null),
        ResolutionPresentation.from(HolderSessionState.Complete.SuccessReason.Denied)
    )

    @Test
    fun `Success carries the presentation derived from its reason by default`() = assertEquals(
        ResolutionPresentation(ResolutionResult.DetailsShared),
        HolderSessionState.Complete.Success().presentation
    )
}
