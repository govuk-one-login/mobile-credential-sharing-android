package uk.gov.onelogin.sharing.verification

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class VerificationResultTest {

    /**
     * DCMAW-20245: AC1: VerificationResult has exactly two states: Success and Failure.
     */
    @Test
    fun `There are only 2 inheritors of 'VerificationResult'`() {
        val expectedInheritors = listOf(
            "Failure",
            "Success"
        )

        val scanResult = ClassGraph()
            .enableAllInfo()
            .acceptPackages(VerificationResult::class.java.packageName)
            .scan()

        val classInfo = scanResult.getClassesImplementing(VerificationResult::class.java)

        assertThat(
            classInfo
            .map(ClassInfo::getSimpleName)
            .toSet(),
            equalTo(expectedInheritors.toSet())
        )
    }
}