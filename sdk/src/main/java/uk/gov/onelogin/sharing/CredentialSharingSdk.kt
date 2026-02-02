package uk.gov.onelogin.sharing

import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

interface CredentialSharingSdk {
    val appGraph: CredentialSharingAppGraph
}
