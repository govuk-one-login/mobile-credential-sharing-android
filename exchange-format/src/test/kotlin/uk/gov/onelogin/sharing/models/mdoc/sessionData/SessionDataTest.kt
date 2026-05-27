package uk.gov.onelogin.sharing.models.mdoc.sessionData

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionDataTest {

    private val emptySessionData = SessionData()
    private val fullSessionData = SessionData(
        data = byteArrayOf(),
        status = SessionDataStatus.SESSION_TERMINATION
    )
    private val okSessionData = fullSessionData.copy(
        status = SessionDataStatus.OK
    )

    @Test
    fun `Can check if an instance has a 'data' property`() {
        assertFalse(emptySessionData.hasData())
        assertTrue(fullSessionData.hasData())
        assertTrue(okSessionData.hasData())
    }

    @Test
    fun `Can check if an instance has a 'status' property`() {
        assertTrue(emptySessionData.hasOkStatus())
        assertFalse(fullSessionData.hasOkStatus())
        assertTrue(okSessionData.hasOkStatus())
    }
}
