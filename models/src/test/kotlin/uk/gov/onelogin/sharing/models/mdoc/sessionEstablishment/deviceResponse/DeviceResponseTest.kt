package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class DeviceResponseTest {

    /**
     * DCMAW-19837: AC2: Enforce DeviceResponse version constraints
     */
    @Test
    fun `Valid responses have '1' as the major version`(
        @TestParameter version: String = testValues(
            "1.0",
            "1.x"
        )
    ) {
        DeviceResponse(
            version = version,
            documents = listOf(),
            documentErrors = mapOf()
        )
    }

    /**
     * DCMAW-19837: AC2: Enforce DeviceResponse version constraints
     */
    @Test
    fun `Invalid versions throw IllegalArgumentExceptions`(
        @TestParameter version: String = testValues(
            "2.0",
            "0.0"
        )
    ) {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            DeviceResponse(
                version = version,
                documents = listOf(),
                documentErrors = mapOf()
            )
        }

        assertThat(
            exception.message,
            equalTo("Received invalid device response version: $version")
        )
    }
}
