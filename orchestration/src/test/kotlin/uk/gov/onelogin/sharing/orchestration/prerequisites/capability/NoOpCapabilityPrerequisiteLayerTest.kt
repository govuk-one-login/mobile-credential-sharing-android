package uk.gov.onelogin.sharing.orchestration.prerequisites.capability

import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite

class NoOpCapabilityPrerequisiteLayerTest {
    private val logger = SystemLogger()
    private val request = Prerequisite.BLUETOOTH
    private val capability by lazy {
        NoOpCapabilityPrerequisiteLayer(logger)
    }

    @After
    fun verifyLogs() {
        assert(
            logger.any {
                it.message.startsWith("Performed $request capability check.")
            }
        )
    }

    @Test
    fun `Prerequisites are always considered capable`() = runTest {
        assertThat(
            capability.checkCapability(request),
            nullValue()
        )
    }
}
