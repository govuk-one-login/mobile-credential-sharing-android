import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.di.SharingAppGraph

class SharingAppGraphStub(private val logger: Logger = SystemLogger()) : SharingAppGraph {
    override fun logger(): Logger = logger
}
