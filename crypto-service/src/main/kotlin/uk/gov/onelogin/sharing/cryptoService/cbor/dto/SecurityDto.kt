package uk.gov.onelogin.sharing.cryptoService.cbor.dto

data class SecurityDto(val cipherSuiteIdentifier: Int, val ephemeralPublicKey: CoseKeyDto)
