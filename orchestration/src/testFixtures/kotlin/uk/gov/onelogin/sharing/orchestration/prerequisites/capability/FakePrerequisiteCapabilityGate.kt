package uk.gov.onelogin.sharing.orchestration.prerequisites.capability

import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer

class FakePrerequisiteCapabilityGate(
    private val result: Map<Prerequisite, MissingPrerequisiteReason.Incapable?>
) : PrerequisiteGateLayer.Capability {

    constructor(
        result: MissingPrerequisiteReason.Incapable? = null
    ) : this(Prerequisite.entries.associateWith { result })

    override fun checkCapability(prerequisite: Prerequisite): MissingPrerequisiteReason.Incapable? =
        result[prerequisite]
}
