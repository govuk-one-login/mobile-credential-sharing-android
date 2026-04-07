package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.v2

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.CameraState

internal class HasCameraState(matcher: Matcher<in CameraState>) :
    HasState<CameraState>(matcher, { (it as? MissingPrerequisiteV2.Camera)?.state })
