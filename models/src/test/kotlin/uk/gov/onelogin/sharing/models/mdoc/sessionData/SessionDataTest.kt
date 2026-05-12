package uk.gov.onelogin.sharing.models.mdoc.sessionData

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class SessionDataTest {

    /**
     * DCMAW-19837: AC1: Enforce SessionData status constraints
     */
    @Test
    fun `Valid session data codes are declared in the status enum`(
        @TestParameter status: SessionDataStatus
    ) {
        SessionData(code = status.code)
    }

    /**
     * DCMAW-19837: AC1: Enforce SessionData status constraints
     */
    @Test
    fun `Invalid session data codes throw IllegalArgumentException instances`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            SessionData(code = 12u)
        }

        assertThat(
            exception.message,
            equalTo("Received invalid session data status: 12")
        )
    }

    /**
     * DCMAW-19837: AC1: Enforce SessionData status constraints
     */
    @Test
    fun `Null session data codes are considered valid`() {
        SessionData(code = null)
    }
}
