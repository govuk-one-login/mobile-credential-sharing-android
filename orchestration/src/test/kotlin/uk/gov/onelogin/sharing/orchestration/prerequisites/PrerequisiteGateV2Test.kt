package uk.gov.onelogin.sharing.orchestration.prerequisites

import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize

class PrerequisiteGateV2Test {

    private val gate by lazy {
        PrerequisiteGateV2()
    }

    @Test
    fun `UNKNOWN prerequisites don't perform checks`() = runTest {
        val result = gate.evaluatePrerequisites(Prerequisite.UNKNOWN)
        assertThat(
            result,
            hasSize(0)
        )
    }

}