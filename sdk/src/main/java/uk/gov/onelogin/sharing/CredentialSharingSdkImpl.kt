package uk.gov.onelogin.sharing

import android.content.Context
import dev.zacsweers.metro.createGraphFactory
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

class CredentialSharingSdkImpl(applicationContext: Context, logger: Logger) : CredentialSharingSdk {

    private val _appGraph: CredentialSharingAppGraph =
        createGraphFactory<CredentialSharingAppGraph.Factory>()
            .create(applicationContext, logger)

    override val appGraph: CredentialSharingAppGraph = _appGraph
}
