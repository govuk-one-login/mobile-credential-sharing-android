package uk.gov.onelogin.sharing.core.implementation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementationData.requiresImplementation
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementationData.requiresImplementationCopy
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementationData.requiresImplementationWithDetail

class RequiresImplementationTest {

    @Test
    fun equalityCheck() {
        assertEquals(requiresImplementation, requiresImplementationCopy)
        assertNotEquals(requiresImplementation, requiresImplementationWithDetail)
    }
}
