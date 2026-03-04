package uk.gov.onelogin.sharing.orchestration.prerequisites

fun interface PrerequisiteGate {
    fun checkPrerequisites(
        prerequisites: Iterable<Prerequisite>
    ): Map<Prerequisite, PrerequisiteResponse>

    fun checkPrerequisites(
        vararg prerequisites: Prerequisite
    ): Map<Prerequisite, PrerequisiteResponse> = checkPrerequisites(
        prerequisites.toList()
    )

    companion object {
        fun Map<Prerequisite, PrerequisiteResponse>.meetsPrerequisites(): Boolean = values.all {
            it == PrerequisiteResponse.MeetsPrerequisites
        }
    }
}
