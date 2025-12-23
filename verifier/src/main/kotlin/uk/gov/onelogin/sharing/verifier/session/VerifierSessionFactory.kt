package uk.gov.onelogin.sharing.verifier.session

import kotlinx.coroutines.CoroutineScope

fun interface VerifierSessionFactory {
    fun create(scope: CoroutineScope): VerifierSession
}
