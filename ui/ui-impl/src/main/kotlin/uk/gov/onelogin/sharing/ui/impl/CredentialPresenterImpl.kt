package uk.gov.onelogin.sharing.ui.impl

import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.ui.api.CredentialPresenter
import uk.gov.onelogin.sharing.ui.api.CredentialProvider

class CredentialPresenterImpl(
    @Suppress("UnusedPrivateProperty")
    private val credentialProvider: CredentialProvider,
    override val appGraph: CredentialSharingAppGraph
) : CredentialPresenter
