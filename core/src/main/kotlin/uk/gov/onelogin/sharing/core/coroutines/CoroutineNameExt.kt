package uk.gov.onelogin.sharing.core.coroutines

import kotlinx.coroutines.CoroutineName

object CoroutineNameExt {
    fun String.asCoroutineName(): CoroutineName = CoroutineName(this)
}