package uk.gov.onelogin.sharing.core

fun interface Actionable<out Action : Any> {
    fun getAction(): Action?
}
