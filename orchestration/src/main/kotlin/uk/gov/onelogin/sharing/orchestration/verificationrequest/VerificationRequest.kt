package uk.gov.onelogin.sharing.orchestration.verificationrequest

import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Parcelize
@Serializable
data class VerificationRequest(val documentType: String, val attributeGroup: AttributeGroup) :
    Parcelable {
    val requestedElements: List<String>
        get() = attributeGroup.attributes.keys.map { it.value }

    companion object {
        fun typed(documentType: DocumentType, attributeGroup: AttributeGroup): VerificationRequest =
            VerificationRequest(
                documentType = documentType.value,
                attributeGroup = attributeGroup
            )

        fun raw(
            documentType: String,
            requestedElements: Map<String, Boolean>
        ): VerificationRequest = VerificationRequest(
            documentType = documentType,
            attributeGroup = AttributeGroup(
                requestedElements.map { (key, retain) ->
                    MdlAttribute.Custom(key) to retain
                }.toMap()
            )
        )

        val VerificationRequestType = object : NavType<VerificationRequest>(
            isNullableAllowed = false
        ) {
            private val jsonConfiguration = Json {
                allowStructuredMapKeys = true
            }

            override fun get(bundle: Bundle, key: String): VerificationRequest? =
                bundle.getString(key)?.let { parseValue(it) }

            override fun put(bundle: Bundle, key: String, value: VerificationRequest) {
                bundle.putString(key, serializeAsValue(value))
            }

            override fun parseValue(value: String): VerificationRequest = jsonConfiguration
                .decodeFromString(value)

            override fun serializeAsValue(value: VerificationRequest): String =
                jsonConfiguration.encodeToString(value)
        }
    }
}
