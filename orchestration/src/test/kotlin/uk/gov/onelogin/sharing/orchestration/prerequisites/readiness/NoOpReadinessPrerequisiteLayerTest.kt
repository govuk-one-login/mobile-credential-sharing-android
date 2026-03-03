package uk.gov.onelogin.sharing.orchestration.prerequisites.readiness

import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite

class NoOpReadinessPrerequisiteLayerTest {
    val readiness by lazy {
        NoOpReadinessPrerequisiteLayer()
    }

    @Test
    fun `Prerequisites are always considered ready`() = runTest {
        assertThat(
            readiness.checkReadiness(Prerequisite.BLUETOOTH),
            nullValue()
        )
    }
}
