package uk.gov.onelogin.sharing.prerequisites.evaluator

fun interface PrerequisiteEvaluator<out Response : Any> {
    fun evaluate(): Response?
}
