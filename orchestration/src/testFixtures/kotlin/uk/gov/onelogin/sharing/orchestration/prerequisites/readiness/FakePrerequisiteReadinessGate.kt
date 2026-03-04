package uk.gov.onelogin.sharing.orchestration.prerequisites.readiness

import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse

class FakePrerequisiteReadinessGate(
    private val result: Map<Prerequisite, PrerequisiteResponse.NotReady?>
) : PrerequisiteGateLayer.Readiness {

    constructor(
        result: PrerequisiteResponse.NotReady? = null
    ) : this(Prerequisite.entries.associateWith { result })

    override fun checkReadiness(prerequisite: Prerequisite): PrerequisiteResponse.NotReady? =
        result[prerequisite]
}
