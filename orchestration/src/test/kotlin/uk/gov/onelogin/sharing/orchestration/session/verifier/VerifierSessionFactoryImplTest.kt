package uk.gov.onelogin.sharing.orchestration.session.verifier

import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import uk.gov.logging.testdouble.SystemLogger

class VerifierSessionFactoryImplTest {
    private val logger = SystemLogger()
    private val sessionFactory = VerifierSessionFactoryImpl(logger)
    private val expectedSession = VerifierSessionImpl(logger)

    @Test
    fun `Factory creates clean Session instances`() = runTest {
        val createdSession = sessionFactory.create()

        assertThat(
            createdSession.currentState.value,
            equalTo(expectedSession.currentState.value)
        )
    }
}
