package uk.gov.onelogin.sharing.holder.mdoc

import kotlinx.coroutines.CoroutineScope

fun interface SessionManagerFactory {
    fun create(scope: CoroutineScope): MdocSessionManager
}
