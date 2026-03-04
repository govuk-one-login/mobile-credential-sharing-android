package uk.gov.onelogin.sharing.orchestration.prerequisites

class StubPrerequisiteGate(private val results: Map<Prerequisite, PrerequisiteResponse>) :
    PrerequisiteGate {
    constructor(
        result: PrerequisiteResponse
    ) : this(Prerequisite.entries.associateWith { result })

    override fun checkPrerequisites(
        prerequisites: Iterable<Prerequisite>
    ): Map<Prerequisite, PrerequisiteResponse> = results
}
