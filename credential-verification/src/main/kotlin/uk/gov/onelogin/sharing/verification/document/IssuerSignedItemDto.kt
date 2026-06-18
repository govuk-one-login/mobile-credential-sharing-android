@file:Suppress("ConstructorParameterNaming")

package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

internal data class IssuerSignedItemDto @JsonCreator constructor(
    @param:JsonProperty(KEY_DIGEST_ID)
    val digestId: Long,
    @param:JsonProperty(KEY_RANDOM)
    val random: ByteArray,
    @param:JsonProperty(KEY_ELEMENT_IDENTIFIER)
    val elementIdentifier: String,
    @param:JsonProperty(KEY_ELEMENT_VALUE)
    val elementValue: Any?
) {
    internal companion object {
        const val KEY_DIGEST_ID = "digestID"
        const val KEY_RANDOM = "random"
        const val KEY_ELEMENT_IDENTIFIER = "elementIdentifier"
        const val KEY_ELEMENT_VALUE = "elementValue"
    }
}
