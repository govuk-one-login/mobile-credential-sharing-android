package uk.gov.onelogin.sharing.orchestration.prerequisites.matchers

import org.hamcrest.Matcher
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState

internal class HasBluetoothState(matcher: Matcher<in BluetoothState>) :
    HasState<BluetoothState>(matcher, { (it as? MissingPrerequisite.Bluetooth)?.state })
