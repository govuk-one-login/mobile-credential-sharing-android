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
        DeviceResponse(version = version)
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
            DeviceResponse(version = version)
        }

        assertThat(
            exception.message,
            equalTo("Received invalid device response version: $version")
        )
    }

    /**
     * DCMAW-19837: AC3: Enforce DeviceResponse status constraints
     */
    @Test
    fun `Valid status codes come from the 'Status' enum`(@TestParameter status: Status) {
        DeviceResponse(statusCode = status.code)
    }

    /**
     * DCMAW-19837: AC3: Enforce DeviceResponse status constraints
     */
    @Test
    fun `Invalid status codes throw IllegalArgumentExceptions`(
        @TestParameter code: Int? = testValues(
            13,
            null
        )
    ) {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            DeviceResponse(statusCode = code)
        }

        assertThat(
            exception.message,
            equalTo("Received invalid device response status code: $code")
        )
    }
}
