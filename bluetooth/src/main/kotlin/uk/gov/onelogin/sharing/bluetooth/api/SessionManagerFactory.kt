package uk.gov.onelogin.sharing.bluetooth.api

import kotlinx.coroutines.CoroutineScope

fun interface SessionManagerFactory {
    fun create(scope: CoroutineScope): MdocSessionManager
}
