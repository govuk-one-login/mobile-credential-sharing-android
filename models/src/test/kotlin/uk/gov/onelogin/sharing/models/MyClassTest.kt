package uk.gov.onelogin.sharing.models

import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Test
import uk.gov.onelogin.sharing.models.DummyObject.myClassInstance

class MyClassTest {
    @Test
    fun createsInstance() {
        assertNotNull(myClassInstance)
    }

    @Test
    fun internalStateIsNullByDefault() {
        assertNull(myClassInstance.state)
    }

    @Test
    fun internalStateIsConfigurable() {
        MyClass(state = "state").run {
            assertNotNull(this.state)
        }
    }
}
