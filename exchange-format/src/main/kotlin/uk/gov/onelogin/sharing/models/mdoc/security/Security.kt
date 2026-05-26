package uk.gov.onelogin.sharing.models.mdoc.security

data class Security(val cipherSuiteIdentifier: Int, val eDeviceKeyBytes: ByteArray)
