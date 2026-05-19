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

    @Test
    fun `Can check if an instance has a 'data' property`() {
        assertFalse(emptySessionData.hasData())
        assertTrue(fullSessionData.hasData())
    }

    @Test
    fun `Can check if an instance has a 'status' property`() {
        assertFalse(emptySessionData.hasErrorStatus())
        assertTrue(fullSessionData.hasErrorStatus())
    }
}
