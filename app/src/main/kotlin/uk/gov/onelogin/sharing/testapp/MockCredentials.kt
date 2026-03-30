package uk.gov.onelogin.sharing.testapp

import android.content.Context
import java.util.Base64
import java.util.UUID

object MockCredentials {

    private val privateKey: String = "-----BEGIN PRIVATE KEY-----\n" +
        "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgdpTO3ZY6wCS8ca3H\n" +
        "B7OOwwKX+4CRNEvgjReT9NiODBKhRANCAATBOw7zuE5KONXusz2EsQJXICpOwwpW\n" +
        "MrZWxlDG/6U1mH8v9LEtfmm4JwMcrYK9Ek0Y19/8FV4SbWyTuiKSNTSa\n" +
        "-----END PRIVATE KEY-----".toByteArray()

    fun mockCredential(context: Context): MockCredential {
        val base64 = context.resources
            .openRawResource(R.raw.mock_credential)
            .bufferedReader()
            .readText()
            .trim()

        return MockCredential(
            id = UUID.randomUUID().toString(),
            displayName = "Jane Doe",
            rawCredential = Base64.getUrlDecoder().decode(base64),
            privateKey = privateKey.toByteArray()
        )
    }
}
