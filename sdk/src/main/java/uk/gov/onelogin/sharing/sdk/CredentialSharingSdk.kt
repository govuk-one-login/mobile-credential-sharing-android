package uk.gov.onelogin.sharing.sdk

import uk.gov.onelogin.sharing.sdk.di.CredentialSharingAppGraph

interface CredentialSharingSdk {
    val appGraph: CredentialSharingAppGraph
}
