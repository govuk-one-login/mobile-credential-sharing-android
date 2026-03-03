package uk.gov.onelogin.sharing.orchestration.prerequisites

fun interface PrerequisiteGate {
    fun checkPrerequisites(
        prerequisites: Collection<Prerequisite>
    ): Map<Prerequisite, PrerequisiteResponse>

    fun checkPrerequisites(
        vararg prerequisites: Prerequisite
    ): Map<Prerequisite, PrerequisiteResponse> = checkPrerequisites(
        prerequisites.toList()
    )
}
