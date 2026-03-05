package uk.gov.onelogin.sharing.orchestration.prerequisites.capability

import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse

class FakePrerequisiteCapabilityGate(
    private val result: Map<Prerequisite, PrerequisiteResponse.Incapable?>
) : PrerequisiteGateLayer.Capability {

    constructor(
        result: PrerequisiteResponse.Incapable? = null
    ) : this(Prerequisite.entries.associateWith { result })

    override fun checkCapability(prerequisite: Prerequisite): PrerequisiteResponse.Incapable? =
        result[prerequisite]
}
