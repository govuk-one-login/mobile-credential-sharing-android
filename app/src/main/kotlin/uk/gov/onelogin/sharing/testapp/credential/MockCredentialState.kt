package uk.gov.onelogin.sharing.testapp.credential

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import java.util.Base64
import java.util.UUID
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import uk.gov.onelogin.sharing.testapp.R

/**
 * Thin variant of [MockCredential] that contains references to data, instead of the actual data.
 *
 * Mappings:
 *
 * - [privateKeyAssetName] -> [MockCredential.privateKey]
 * - [rawCredentialRes] -> [MockCredential.rawCredential]
 *
 * @sample uk.gov.onelogin.sharing.testapp.credential.MockCredentialState.toCredential
 */
@Parcelize
@Serializable
data class MockCredentialState(
    val displayName: String,
    val id: String = UUID.randomUUID().toString(),
    private val privateKeyAssetName: String = "test_private_key.pem",
    private val rawCredentialRes: Int = R.raw.mock_credential
) : Parcelable {

    override fun toString(): String = "MockCredentialState(id=$id, displayName=$displayName)"

    fun toCredential(context: Context): MockCredential {
        val privateKey = context.assets.open(privateKeyAssetName)
            .bufferedReader()
            .readText()
            .toByteArray()

        val base64EncodedRawCredential = context.resources
            .openRawResource(rawCredentialRes)
            .bufferedReader()
            .readText()
            .trim()

        return MockCredential(
            id = this.id,
            displayName = this.displayName,
            rawCredential = Base64.getUrlDecoder().decode(base64EncodedRawCredential),
            privateKey = privateKey
        )
    }

    companion object {
        val MockCredentialStateType: NavType<MockCredentialState> =
            object : NavType<MockCredentialState>(
                isNullableAllowed = false
            ) {
                override fun get(bundle: Bundle, key: String): MockCredentialState? =
                    bundle.getString(key)?.let { parseValue(it) }

                override fun put(bundle: Bundle, key: String, value: MockCredentialState) {
                    bundle.putString(key, serializeAsValue(value))
                }

                override fun parseValue(value: String): MockCredentialState =
                    Json.decodeFromString(value)

                override fun serializeAsValue(value: MockCredentialState): String =
                    Json.encodeToString(value)
            }
    }
}
