package uk.gov.onelogin.sharing.orchestration.session

import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * Abstract test harness for providing functional testing capabilities for [SessionFactory]
 * implementations.
 */
interface SessionFactoryImplTest<Session : Any> {
    val factory: SessionFactory<Session>
    val assertion: Matcher<Session>

    @Test
    fun factoryCreatesCleanSessionInstances(): TestResult = runTest {
        assertThat(
            factory.create(),
            assertion
        )
    }
}
