package uk.gov.onelogin.sharing.orchestration.prerequisites.capability

import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite

class NoOpCapabilityPrerequisiteLayerTest {
    val capability by lazy {
        NoOpCapabilityPrerequisiteLayer()
    }

    @Test
    fun `Prerequisites are always considered capable`() = runTest {
        assertThat(
            capability.checkCapability(Prerequisite.BLUETOOTH),
            nullValue()
        )
    }
}
