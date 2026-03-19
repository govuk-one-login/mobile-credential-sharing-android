package uk.gov.onelogin.sharing.cryptoService.cryptography

import kotlinx.io.bytestring.buildByteString
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.NIST_INITIALISATION_VECTOR_SIZE
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.append

fun createNistInitialisationVector(roleBytes: UInt, messageCounterBytes: UInt) =
    buildByteString(NIST_INITIALISATION_VECTOR_SIZE) {
        append(0u)
        append(roleBytes)
        append(messageCounterBytes)
    }.toByteArray()
