package uk.gov.onelogin.sharing.orchestration.session

import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows

class FakeSessionFactoryTest {

    private val factory by lazy {
        FakeSessionFactory(
            "This is a unit test",
            "This is another unit test"
        )
    }

    @Test
    fun `Provides different instances based on constructor parameters`() = runTest {
        assertThat(
            factory.create(),
            not(equalTo(factory.create()))
        )
    }

    @Test
    fun `Throws exceptions when accessing state before creation`() = runTest {
        assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            factory.getCurrentSession()
        }
    }

    @Test
    fun `Throws exceptions when 'creating' more instances than what's available`() = runTest {
        factory.create()
        factory.create()

        assertThrows(ArrayIndexOutOfBoundsException::class.java) {
            factory.create()
        }
    }
}
