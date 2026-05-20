package uk.gov.onelogin.sharing.verification

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasProperty
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class VerificationResultTest {

    private val classGraphConfig = ClassGraph()
        .enableAllInfo()

    /**
     * DCMAW-20245: AC1: [VerificationResult] has exactly two states: Success and Failure.
     */
    @Test
    fun `There are only 2 inheritors of 'VerificationResult'`() {
        val expectedInheritors = listOf(
            "Failure",
            "Success"
        )

        val scanResult = classGraphConfig
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

    /**
     * DCMAW-20245: AC2: [VerificationResult.Failure] carries a single [VerificationError] value.
     */
    @Test
    fun `Failures only have a single property`() {
        val scanResult = classGraphConfig
            .acceptClasses(
                VerificationResult.Failure::class.java.name,
                VerificationError::class.java.name,
            )
            .scan()
            .getClassInfo(VerificationResult.Failure::class.java.name)

        assertThat(
            scanResult.fieldInfo.size,
            equalTo(1)
        )

        val fieldInfo = scanResult.fieldInfo[0]

        assertThat(
            fieldInfo,
            allOf(
                hasProperty(
                    "name",
                    equalTo("error")
                ),
                hasProperty(
                    "typeDescriptorStr",
                    containsString(VerificationError::class.java.name.replace(".", "/"))
                )
            )
        )
    }

    /**
     * DCMAW-20245: AC3: [VerificationResult.Failure] is usable as a throwable error type.
     */
    @Test
    fun `Failures are considered to be throwable`(
        @TestParameter error: VerificationError
    ) {
        val expected = VerificationResult.Failure(error)

        val actual = assertThrows(VerificationResult.Failure::class.java) {
            throw expected
        }

        assertThat(
            actual,
            equalTo(expected)
        )
    }
}
