package uk.gov.onelogin.sharing.prerequisites

fun interface Actionable<out Action : Any> {
    fun getAction(): Action?
}
