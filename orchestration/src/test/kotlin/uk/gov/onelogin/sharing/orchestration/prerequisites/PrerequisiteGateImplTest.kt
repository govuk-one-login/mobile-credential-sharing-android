package uk.gov.onelogin.sharing.orchestration.prerequisites

import android.Manifest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasEntry
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.FakePrerequisiteAuthorizationGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.PrerequisiteResponseMatchers.hasUnauthorizedPermissions

class PrerequisiteGateImplTest {

    private var authorizationResult: PrerequisiteResponse.Unauthorized? = null
    private val prerequisite = Prerequisite.BLUETOOTH

    private val authorization by lazy {
        FakePrerequisiteAuthorizationGate(
            result = authorizationResult
        )
    }
    private val gate by lazy {
        PrerequisiteGateImpl(
            authorization = authorization
        )
    }

    @Test
    fun `Meets all prerequisites`() = runTest {
        val result = gate.checkPrerequisites(prerequisite)
        assertThat(
            result,
            hasEntry(
                prerequisite,
                PrerequisiteResponse.MeetsPrerequisites,
            )
        )
    }

    @Test
    fun `Failing authorization provides an authorized value`() = runTest {
        authorizationResult = PrerequisiteResponse.Unauthorized(
            UnauthorizedReason.MissingPermissions(Manifest.permission.BLUETOOTH)
        )

        assertThat(
            gate.checkPrerequisites(prerequisite),
            hasEntry(
                equalTo(prerequisite),
                hasUnauthorizedPermissions(
                        contains(Manifest.permission.BLUETOOTH),
                )
            )
        )
    }
}
