package uk.gov.onelogin.sharing.orchestration.holder.session

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs.engagementPresentationStub

class EngagementPresentationTest {

    @Test
    fun `withoutQr clears the payload and retains the cancellation rule`() {
        val presentation = EngagementPresentation(
            qrPayload = "qr",
            cancelRequiresConfirmation = false
        )
        val hidden = presentation.withoutQr()

        assertNull(hidden.qrPayload)
        assertEquals(presentation.cancelRequiresConfirmation, hidden.cancelRequiresConfirmation)
    }

    @Test
    fun `Engagement presentation cancellation rule matches the PresentingEngagement state`() {
        val state = HolderSessionState.PresentingEngagement(
            engagementPresentationStub.copy(qrPayload = "qr")
        )

        assertEquals(
            state.userCanCancel(),
            state.presentation.cancelRequiresConfirmation != null
        )
        assertEquals(
            state.shouldConfirmCancellation(),
            state.presentation.cancelRequiresConfirmation
        )
    }
}
