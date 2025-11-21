package uk.gov.onelogin.sharing.security.cbor.dto

data class SecurityDto(val cipherSuiteIdentifier: Int, val ephemeralPublicKey: CoseKeyDto)
