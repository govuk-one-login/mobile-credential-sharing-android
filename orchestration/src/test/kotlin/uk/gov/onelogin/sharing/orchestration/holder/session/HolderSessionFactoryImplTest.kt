package uk.gov.onelogin.sharing.orchestration.holder.session

import org.hamcrest.Matcher
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers
import uk.gov.onelogin.sharing.orchestration.session.SessionFactoryImplTest
import uk.gov.onelogin.sharing.orchestration.session.StateContainer
import uk.gov.onelogin.sharing.orchestration.session.matchers.StateContainerMatchers

class HolderSessionFactoryImplTest :
    SessionFactoryImplTest<StateContainer<in HolderSessionState>> {

    private val logger = SystemLogger()
    override val factory: HolderSessionFactory = HolderSessionFactory(logger)
    override val assertion: Matcher<StateContainer<in HolderSessionState>> =
        StateContainerMatchers.hasCurrentState(HolderSessionStateMatchers.isNotStarted())
}
