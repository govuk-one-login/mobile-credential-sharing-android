package uk.gov.onelogin.sharing.testapp.credential

object MockCredentialData {
    val mockCredentialState = MockCredentialState(displayName = "Jane Doe")

    val mockPrivateKeyPem = """
        -----BEGIN PRIVATE KEY-----
        MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgdpTO3ZY6wCS8ca3H
        B7OOwwKX+4CRNEvgjReT9NiODBKhRANCAATBOw7zuE5KONXusz2EsQJXICpOwwpW
        MrZWxlDG/6U1mH8v9LEtfmm4JwMcrYK9Ek0Y19/8FV4SbWyTuiKSNTSa
        -----END PRIVATE KEY-----
    """.trimIndent()
}
