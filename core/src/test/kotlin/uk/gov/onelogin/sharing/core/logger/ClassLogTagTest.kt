package uk.gov.onelogin.sharing.core.logger

import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.core.data.UriTestData

class ClassLogTagTest {
    @Test
    fun `logTag works for an object instance`() {
        assertEquals("UriTestData", UriTestData.logTag)
    }
}
