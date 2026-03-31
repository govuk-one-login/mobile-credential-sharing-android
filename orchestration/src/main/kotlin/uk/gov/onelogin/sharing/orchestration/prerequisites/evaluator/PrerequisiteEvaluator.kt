package uk.gov.onelogin.sharing.orchestration.prerequisites.evaluator

fun interface PrerequisiteEvaluator<out Response : Any> {
    fun evaluate(): Response?
}
