package uk.gov.onelogin.sharing.testapp

import uk.gov.onelogin.sharing.CredentialSharingSdk
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

class FakeCredentialSharingSdk(override val appGraph: CredentialSharingAppGraph) :
    CredentialSharingSdk
