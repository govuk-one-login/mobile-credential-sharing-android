package uk.gov.onelogin.sharing.orchestration.prerequisites

/**
 * Abstraction for performing various forms of checks based on [Prerequisite] input.
 *
 * @see PrerequisiteGateLayer
 */
fun interface PrerequisiteGate<out Response : Any> {
    fun evaluatePrerequisites(prerequisites: Iterable<Prerequisite>): List<Response>

    fun evaluatePrerequisites(vararg prerequisites: Prerequisite): List<Response> =
        evaluatePrerequisites(
            prerequisites.toList()
        )

    interface V1 : PrerequisiteGate<MissingPrerequisite>
    interface V2 : PrerequisiteGate<MissingPrerequisiteV2>
}
