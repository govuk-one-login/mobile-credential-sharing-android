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
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.FakePrerequisiteCapabilityGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReasonMatchers.isMissingHardware
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.PrerequisiteResponseMatchers.hasIncapableReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.PrerequisiteResponseMatchers.hasUnauthorizedPermissions

class PrerequisiteGateImplTest {

    private var authorizationResult: PrerequisiteResponse.Unauthorized? = null
    private var capabilityResult: PrerequisiteResponse.Incapable? = null

    private val prerequisite = Prerequisite.BLUETOOTH

    private val authorization by lazy {
        FakePrerequisiteAuthorizationGate(
            result = authorizationResult
        )
    }
    private val capability by lazy {
        FakePrerequisiteCapabilityGate(
            result = capabilityResult
        )
    }
    private val gate by lazy {
        PrerequisiteGateImpl(
            authorization = authorization,
            capability = capability,
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
    fun `Failing authorization provides an unauthorized value`() = runTest {
        setupAuthorizationFailure()

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

    @Test
    fun `Authorization failures are a higher priority than capability failures`() = runTest {
        setupAuthorizationFailure()
        setupCapabilityFailure()

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

    @Test
    fun `Failing capability provides an incapable reason`() = runTest {
        setupCapabilityFailure()
        assertThat(
            gate.checkPrerequisites(prerequisite),
            hasEntry(
                equalTo(prerequisite),
                hasIncapableReason(isMissingHardware())
            )
        )
    }

    private fun setupAuthorizationFailure(
        reason: UnauthorizedReason = UnauthorizedReason.MissingPermissions(
            Manifest.permission.BLUETOOTH
        )
    ) {
        authorizationResult = PrerequisiteResponse.Unauthorized(reason)
    }

    private fun setupCapabilityFailure(
        reason: IncapableReason = IncapableReason.MissingHardware
    ) {
        capabilityResult = PrerequisiteResponse.Incapable(reason)
    }
}
