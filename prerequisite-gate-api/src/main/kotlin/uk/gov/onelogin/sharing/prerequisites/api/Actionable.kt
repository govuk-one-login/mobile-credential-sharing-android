package uk.gov.onelogin.sharing.prerequisites.api

/**
 * Implementations of this functional interface expose an [Action].
 */
fun interface Actionable<out Action : Any> {
    fun getAction(): Action?
}
