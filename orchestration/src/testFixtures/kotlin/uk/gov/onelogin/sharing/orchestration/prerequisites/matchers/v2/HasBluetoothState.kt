package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers.v2

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState

internal class HasBluetoothState(matcher: Matcher<in BluetoothState>) :
    HasState<BluetoothState>(matcher, { (it as? MissingPrerequisiteV2.Bluetooth)?.state })
