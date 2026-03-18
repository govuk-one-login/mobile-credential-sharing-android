package uk.gov.onelogin.sharing.orchestration.holder.session

import org.hamcrest.Matcher
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers
import uk.gov.onelogin.sharing.orchestration.session.SessionFactoryImplTest
import uk.gov.onelogin.sharing.orchestration.session.StateContainer
import uk.gov.onelogin.sharing.orchestration.session.matchers.StateContainerMatchers
import uk.gov.onelogin.sharing.security.FakeSessionSecurity
import uk.gov.onelogin.sharing.security.engagement.FakeEngagementGenerator

class HolderSessionFactoryImplTest :
    SessionFactoryImplTest<StateContainer<in HolderSessionState>> {

    private val logger = SystemLogger()
    override val factory: HolderSessionFactory = HolderSessionFactory(
        logger = logger,
        sessionSecurity = FakeSessionSecurity(),
        engagementGenerator = FakeEngagementGenerator("")
    )
    override val assertion: Matcher<StateContainer<in HolderSessionState>> =
        StateContainerMatchers.hasCurrentState(HolderSessionStateMatchers.isNotStarted())
}
