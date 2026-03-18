package uk.gov.onelogin.sharing.orchestration.prerequisites

import android.Manifest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasEntry
import org.junit.After
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.FakePrerequisiteAuthorizationGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.FakePrerequisiteCapabilityGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReasonMatchers.isMissingHardware
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.PrerequisiteResponseMatchers.hasIncapableReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.PrerequisiteResponseMatchers.hasNotReadyReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.PrerequisiteResponseMatchers.hasUnauthorizedPermissions
import uk.gov.onelogin.sharing.orchestration.prerequisites.readiness.FakePrerequisiteReadinessGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.readiness.NotReadyReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.readiness.NotReadyReasonMatchers.hasBluetoothTurnedOff

class PrerequisiteGateImplTest {

    private var authorizationResult: MutableMap<Prerequisite, PrerequisiteResponse.Unauthorized?> =
        mutableMapOf()
    private var capabilityResult: MutableMap<Prerequisite, PrerequisiteResponse.Incapable?> =
        mutableMapOf()
    private var readinessResult: MutableMap<Prerequisite, PrerequisiteResponse.NotReady?> =
        mutableMapOf()

    private val prerequisite = Prerequisite.BLUETOOTH
    private val logger = SystemLogger()
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
    private val readiness by lazy {
        FakePrerequisiteReadinessGate(
            result = readinessResult
        )
    }
    private val gate by lazy {
        PrerequisiteGateImpl(
            authorization = authorization,
            capability = capability,
            logger = logger,
            readiness = readiness
        )
    }

    @After
    fun verifyLogs() {
        assert(
            logger.any {
                it.message.startsWith("Performed prerequisite checks for: ")
            }
        )
    }

    @Test
    fun `Response maps to provided Prerequisites`() = runTest {
        val prerequisites = listOf(
            Prerequisite.BLUETOOTH,
            Prerequisite.CAMERA
        )
        val result = gate.checkPrerequisites(prerequisites)

        assertThat(
            result.size,
            equalTo(prerequisites.size)
        )
    }

    @Test
    fun `Responses are grouped based on prerequisite`() = runTest {
        setupCapabilityFailure(
            prerequisite = Prerequisite.CAMERA
        )
        val result = gate.checkPrerequisites(Prerequisite.entries)

        assertThat(
            result,
            allOf(
                hasEntry(
                    equalTo(Prerequisite.CAMERA),
                    hasIncapableReason(isMissingHardware())
                ),
                hasEntry(
                    equalTo(Prerequisite.BLUETOOTH),
                    equalTo(PrerequisiteResponse.MeetsPrerequisites)
                )
            )
        )
    }

    @Test
    fun `Meets all prerequisites`() = runTest {
        val result = gate.checkPrerequisites(prerequisite)
        assertThat(
            result,
            hasEntry(
                prerequisite,
                PrerequisiteResponse.MeetsPrerequisites
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
                    contains(Manifest.permission.BLUETOOTH)
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
                    contains(Manifest.permission.BLUETOOTH)
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

    @Test
    fun `Capability failures are a higher priority than readiness failures`() = runTest {
        setupCapabilityFailure()
        setupReadinessFailure()

        assertThat(
            gate.checkPrerequisites(prerequisite),
            hasEntry(
                equalTo(prerequisite),
                hasIncapableReason(isMissingHardware())
            )
        )
    }

    @Test
    fun `Failing readiness provides a not ready reason`() = runTest {
        setupReadinessFailure()
        assertThat(
            gate.checkPrerequisites(prerequisite),
            hasEntry(
                equalTo(prerequisite),
                hasNotReadyReason(hasBluetoothTurnedOff())
            )
        )
    }

    private fun setupAuthorizationFailure(
        prerequisite: Prerequisite = this.prerequisite,
        reason: UnauthorizedReason = UnauthorizedReason.MissingPermissions(
            Manifest.permission.BLUETOOTH
        )
    ) {
        authorizationResult[prerequisite] = PrerequisiteResponse.Unauthorized(reason)
    }

    private fun setupCapabilityFailure(
        prerequisite: Prerequisite = this.prerequisite,
        reason: IncapableReason = IncapableReason.MissingHardware
    ) {
        capabilityResult[prerequisite] = PrerequisiteResponse.Incapable(reason)
    }

    private fun setupReadinessFailure(
        prerequisite: Prerequisite = this.prerequisite,
        reason: NotReadyReason = NotReadyReason.BluetoothTurnedOff
    ) {
        readinessResult[prerequisite] = PrerequisiteResponse.NotReady(reason)
    }
}
