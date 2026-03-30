package uk.gov.onelogin.sharing.orchestration.prerequisites

import android.Manifest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.After
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.FakePrerequisiteAuthorizationGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.UnauthorizedReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.FakePrerequisiteCapabilityGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReasonMatchers.isMissingHardware
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.MissingPrerequisiteMatchers.hasPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.MissingPrerequisiteMatchers.hasReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.PrerequisiteResponseMatchers.hasIncapableReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.PrerequisiteResponseMatchers.hasNotReadyReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.PrerequisiteResponseMatchers.hasUnauthorizedPermissions
import uk.gov.onelogin.sharing.orchestration.prerequisites.readiness.FakePrerequisiteReadinessGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.readiness.NotReadyReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.readiness.NotReadyReasonMatchers.hasBluetoothTurnedOff

class PrerequisiteGateImplTest {

    private var authorizationResult:
        MutableMap<Prerequisite, MissingPrerequisiteReason.Unauthorized?> =
        mutableMapOf()
    private var capabilityResult: MutableMap<Prerequisite, MissingPrerequisiteReason.Incapable?> =
        mutableMapOf()
    private var readinessResult: MutableMap<Prerequisite, MissingPrerequisiteReason.NotReady?> =
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
    fun `Missing prerequisites are within a list`() = runTest {
        setupCapabilityFailure(
            prerequisite = Prerequisite.CAMERA
        )
        val result = gate.evaluatePrerequisites(Prerequisite.entries)

        assertThat(
            result,
            allOf(
                hasSize(1),
                contains(
                    allOf(
                        hasPrerequisite(Prerequisite.CAMERA),
                        hasReason(hasIncapableReason(isMissingHardware()))
                    )
                )
            )
        )
    }

    @Test
    fun `Meeting all prerequisites returns an empty list`() = runTest {
        val result = gate.evaluatePrerequisites(prerequisite)
        assertThat(
            result,
            hasSize(0)
        )
    }

    @Test
    fun `Failing authorization provides an unauthorized value`() = runTest {
        setupAuthorizationFailure()

        assertThat(
            gate.evaluatePrerequisites(prerequisite),
            contains(
                allOf(
                    hasPrerequisite(prerequisite),
                    hasReason(
                        hasUnauthorizedPermissions(
                            contains(Manifest.permission.BLUETOOTH)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Authorization failures are a higher priority than capability failures`() = runTest {
        setupAuthorizationFailure()
        setupCapabilityFailure()

        assertThat(
            gate.evaluatePrerequisites(prerequisite),
            contains(
                allOf(
                    hasPrerequisite(prerequisite),
                    hasReason(
                        hasUnauthorizedPermissions(
                            contains(Manifest.permission.BLUETOOTH)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Failing capability provides an incapable reason`() = runTest {
        setupCapabilityFailure()

        assertThat(
            gate.evaluatePrerequisites(prerequisite),
            contains(
                allOf(
                    hasPrerequisite(prerequisite),
                    hasReason(
                        hasIncapableReason(isMissingHardware())
                    )
                )
            )
        )
    }

    @Test
    fun `Capability failures are a higher priority than readiness failures`() = runTest {
        setupCapabilityFailure()
        setupReadinessFailure()

        assertThat(
            gate.evaluatePrerequisites(prerequisite),
            contains(
                allOf(
                    hasPrerequisite(prerequisite),
                    hasReason(
                        hasIncapableReason(isMissingHardware())
                    )
                )
            )
        )
    }

    @Test
    fun `Failing readiness provides a not ready reason`() = runTest {
        setupReadinessFailure()

        assertThat(
            gate.evaluatePrerequisites(prerequisite),
            contains(
                allOf(
                    hasPrerequisite(prerequisite),
                    hasReason(
                        hasNotReadyReason(hasBluetoothTurnedOff())
                    )
                )
            )
        )
    }

    private fun setupAuthorizationFailure(
        prerequisite: Prerequisite = this.prerequisite,
        reason: UnauthorizedReason = UnauthorizedReason.MissingPermissions(
            Manifest.permission.BLUETOOTH
        )
    ) {
        authorizationResult[prerequisite] = MissingPrerequisiteReason.Unauthorized(reason)
    }

    private fun setupCapabilityFailure(
        prerequisite: Prerequisite = this.prerequisite,
        reason: IncapableReason = IncapableReason.MissingHardware
    ) {
        capabilityResult[prerequisite] = MissingPrerequisiteReason.Incapable(reason)
    }

    private fun setupReadinessFailure(
        prerequisite: Prerequisite = this.prerequisite,
        reason: NotReadyReason = NotReadyReason.BluetoothTurnedOff
    ) {
        readinessResult[prerequisite] = MissingPrerequisiteReason.NotReady(reason)
    }
}
