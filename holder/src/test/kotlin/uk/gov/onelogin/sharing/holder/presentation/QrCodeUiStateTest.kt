package uk.gov.onelogin.sharing.holder.presentation

import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs

class QrCodeUiStateTest {

    @Test
    fun `Takes the QR payload from engagement presentation`() {
        val presentation = HolderSessionStateStubs.engagementPresentationStub
        assertEquals(
            presentation.qrPayload,
            QrCodeUiState(presentation).qrPayload
        )
    }

    @Test
    fun `A hidden engagement presentation has no QR payload`() {
        val presentation = HolderSessionStateStubs.hiddenEngagementPresentationStub
        assertNull(QrCodeUiState(presentation).qrPayload)
    }

    @Test
    fun `Defaults to a hidden QR code`() {
        assertNull(QrCodeUiState().qrPayload)
    }
}
