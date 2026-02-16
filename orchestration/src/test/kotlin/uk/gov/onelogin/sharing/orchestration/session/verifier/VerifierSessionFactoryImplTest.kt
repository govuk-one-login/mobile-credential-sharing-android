package uk.gov.onelogin.sharing.orchestration.session.verifier

import org.hamcrest.Matcher
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.session.SessionFactoryImplTest
import uk.gov.onelogin.sharing.orchestration.session.StateContainer
import uk.gov.onelogin.sharing.orchestration.session.matchers.StateContainerMatchers.hasCurrentState
import uk.gov.onelogin.sharing.orchestration.session.verifier.matchers.VerifierSessionStateMatchers.isNotStarted

class VerifierSessionFactoryImplTest :
    SessionFactoryImplTest<StateContainer<in VerifierSessionState>> {

    private val logger = SystemLogger()
    override val factory: VerifierSessionFactory = VerifierSessionFactory(logger)
    override val assertion: Matcher<StateContainer<in VerifierSessionState>> =
        hasCurrentState(isNotStarted())
}
