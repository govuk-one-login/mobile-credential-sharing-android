import android.content.Context
import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

class CredentialSharingAppGraphStub(
    private val applicationContext: Context,
    private val logger: Logger = SystemLogger()
) : CredentialSharingAppGraph {
    override fun applicationContext(): Context = applicationContext

    override fun logger(): Logger = logger
}
