package uk.gov.onelogin.sharing.orchestration.session.holder

import org.hamcrest.Matcher
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.session.SessionFactoryImplTest
import uk.gov.onelogin.sharing.orchestration.session.StateContainer
import uk.gov.onelogin.sharing.orchestration.session.holder.matchers.HolderSessionStateMatchers.isNotStarted
import uk.gov.onelogin.sharing.orchestration.session.matchers.StateContainerMatchers.hasCurrentState

class HolderSessionFactoryImplTest :
    SessionFactoryImplTest<StateContainer<in HolderSessionState>> {

    private val logger = SystemLogger()
    override val factory: HolderSessionFactory = HolderSessionFactory(logger)
    override val assertion: Matcher<StateContainer<in HolderSessionState>> =
        hasCurrentState(isNotStarted())
}
