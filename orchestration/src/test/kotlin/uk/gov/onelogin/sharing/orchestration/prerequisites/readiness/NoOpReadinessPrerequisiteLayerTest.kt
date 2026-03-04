package uk.gov.onelogin.sharing.orchestration.prerequisites.readiness

import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite

class NoOpReadinessPrerequisiteLayerTest {
    private val logger = SystemLogger()
    private val request = Prerequisite.BLUETOOTH
    val readiness by lazy {
        NoOpReadinessPrerequisiteLayer(logger)
    }

    @Test
    fun `Prerequisites are always considered ready`() = runTest {
        assertThat(
            readiness.checkReadiness(request),
            nullValue()
        )
    }
}
