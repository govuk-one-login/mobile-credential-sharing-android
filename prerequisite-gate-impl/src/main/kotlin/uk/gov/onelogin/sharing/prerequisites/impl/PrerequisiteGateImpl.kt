package uk.gov.onelogin.sharing.prerequisites.impl

import uk.gov.onelogin.sharing.core.SharingSessionScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.prerequisites.api.MissingPrerequisite
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteGate
import uk.gov.onelogin.sharing.prerequisites.api.evaluator.PrerequisiteEvaluator
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.api.state.CameraState
import uk.gov.onelogin.sharing.prerequisites.api.state.LocationState

@ContributesBinding(SharingSessionScope::class)
@Inject
class PrerequisiteGateImpl(
    private val bluetoothEvaluator: PrerequisiteEvaluator<BluetoothState>,
    private val cameraEvaluator: PrerequisiteEvaluator<CameraState>,
    private val locationEvaluator: PrerequisiteEvaluator<LocationState>,
    private val logger: Logger
) : PrerequisiteGate {

    override fun evaluatePrerequisites(
        prerequisites: Iterable<Prerequisite>
    ): List<MissingPrerequisite> = prerequisites.mapNotNull { prerequisite ->
        when (prerequisite) {
            Prerequisite.BLUETOOTH -> bluetoothEvaluator.evaluate()
                ?.let(MissingPrerequisites::Bluetooth)

            Prerequisite.CAMERA -> cameraEvaluator.evaluate()
                ?.let(MissingPrerequisites::Camera)

            Prerequisite.LOCATION -> locationEvaluator.evaluate()
                ?.let(MissingPrerequisites::Location)

            else -> null
        }.also {
            logger.debug(
                PrerequisiteGateImpl::class.java.simpleName,
                "Performed prerequisite checks for: $prerequisites"
            )
        }
    }
}
