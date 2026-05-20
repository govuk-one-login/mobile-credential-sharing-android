package uk.gov.onelogin.sharing.verification

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.github.classgraph.ClassGraph
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class VerificationResultTest {

    private val resultInheritorCount = 2

    /**
     * DCMAW-20245: AC1: VerificationResult has exactly two states: Success and Failure.
     */
    @Test
    fun `There are only 2 inheritors of 'VerificationResult'`() {
        val scanResult = ClassGraph()
            .enableAllInfo()
            .acceptPackages(VerificationResult::class.java.packageName)
            .scan()

        val classInfo = scanResult.getClassesImplementing(VerificationResult::class.java)

        assertThat(
            classInfo.size,
            equalTo(resultInheritorCount)
        )

        listOf(
            "Failure",
            "Success"
        ).forEach { expected ->
            assertTrue(
                "Cannot find the expected '$expected' inheritor",
                classInfo.any { expected == it.simpleName }
            )
        }
    }
}