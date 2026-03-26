package uk.gov.onelogin.sharing.orchestration.prerequisites.readiness

import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer

class FakePrerequisiteReadinessGate(
    private val result: Map<Prerequisite, MissingPrerequisiteReason.NotReady?>
) : PrerequisiteGateLayer.Readiness {

    constructor(
        result: MissingPrerequisiteReason.NotReady? = null
    ) : this(Prerequisite.entries.associateWith { result })

    override fun checkReadiness(prerequisite: Prerequisite): MissingPrerequisiteReason.NotReady? =
        result[prerequisite]
}
