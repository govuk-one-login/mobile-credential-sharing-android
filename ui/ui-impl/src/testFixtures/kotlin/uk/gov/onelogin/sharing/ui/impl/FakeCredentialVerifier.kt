package uk.gov.onelogin.sharing.ui.impl

import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.ui.api.CredentialVerifier

class FakeCredentialVerifier(override val appGraph: CredentialSharingAppGraph) : CredentialVerifier
