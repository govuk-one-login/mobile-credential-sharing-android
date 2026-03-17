package uk.gov.onelogin.sharing.orchestration.verifier.session

import org.hamcrest.Matcher
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.orchestration.session.SessionFactoryImplTest
import uk.gov.onelogin.sharing.orchestration.session.StateContainer
import uk.gov.onelogin.sharing.orchestration.session.matchers.StateContainerMatchers
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers

class VerifierSessionFactoryImplTest :
    SessionFactoryImplTest<StateContainer<in VerifierSessionState>> {

    private val logger = SystemLogger()
    override val factory: VerifierSessionFactory = VerifierSessionFactory(logger)
    override val assertion: Matcher<StateContainer<in VerifierSessionState>> =
        StateContainerMatchers.hasCurrentState(VerifierSessionStateMatchers.isNotStarted())
}
