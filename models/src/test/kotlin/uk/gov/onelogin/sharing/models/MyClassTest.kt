package uk.gov.onelogin.sharing.models

import junit.framework.TestCase.assertNotNull
import org.junit.Test
import uk.gov.onelogin.sharing.models.DummyObject.myClassInstance

class MyClassTest {
    @Test
    fun createsInstance() {
        assertNotNull(myClassInstance)
    }
}