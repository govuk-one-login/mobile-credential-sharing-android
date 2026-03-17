package uk.gov.onelogin.sharing.testapp

import uk.gov.onelogin.sharing.sdk.CredentialSharingSdk
import uk.gov.onelogin.sharing.sdk.di.CredentialSharingAppGraph

class FakeCredentialSharingSdk(override val appGraph: CredentialSharingAppGraph) :
    CredentialSharingSdk
