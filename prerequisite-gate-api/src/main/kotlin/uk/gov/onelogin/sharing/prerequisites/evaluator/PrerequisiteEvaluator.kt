package uk.gov.onelogin.sharing.prerequisites.evaluator

/**
 * Typed functional interface for evaluating a [uk.gov.onelogin.sharing.prerequisites.Prerequisite].
 *
 * Implementations internally decide which Prerequisite gets evaluated, though the [Response] type
 * infers which Prerequisite is being evaluated.
 *
 * @param Response The return type of the [evaluate] function.
 */
fun interface PrerequisiteEvaluator<out Response : Any> {
    fun evaluate(): Response?
}
