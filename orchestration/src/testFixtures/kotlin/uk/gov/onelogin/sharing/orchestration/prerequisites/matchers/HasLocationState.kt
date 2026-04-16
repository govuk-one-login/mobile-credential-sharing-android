package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.LocationState

internal class HasLocationState(matcher: Matcher<in LocationState>) :
    HasState<LocationState>(matcher, { (it as? MissingPrerequisite.Location)?.state })
