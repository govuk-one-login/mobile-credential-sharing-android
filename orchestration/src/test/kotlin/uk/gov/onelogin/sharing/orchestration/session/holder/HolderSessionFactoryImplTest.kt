package uk.gov.onelogin.sharing.orchestration.session.holder

import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import uk.gov.logging.testdouble.SystemLogger

class HolderSessionFactoryImplTest {
    private val logger = SystemLogger()
    private val sessionFactory = HolderSessionFactory(logger)
    private val expectedSession = HolderSessionImpl(logger)

    @Test
    fun `Factory creates clean Session instances`() = runTest {
        val createdSession = sessionFactory.create()

        assertThat(
            createdSession.currentState.value,
            equalTo(expectedSession.currentState.value)
        )
    }
}
