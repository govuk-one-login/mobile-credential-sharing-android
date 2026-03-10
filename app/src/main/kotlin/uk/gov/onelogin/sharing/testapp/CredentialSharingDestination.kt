package uk.gov.onelogin.sharing.testapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
sealed interface CredentialSharingDestination : Parcelable {
    @Parcelize
    @Serializable
    data object Holder : CredentialSharingDestination

    @Parcelize
    @Serializable
    data object Verifier : CredentialSharingDestination
}
