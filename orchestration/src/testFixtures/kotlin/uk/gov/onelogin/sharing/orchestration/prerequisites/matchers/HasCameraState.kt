package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.CameraState

internal class HasCameraState(matcher: Matcher<in CameraState>) :
    HasState<CameraState>(matcher, { (it as? MissingPrerequisite.Camera)?.state })
