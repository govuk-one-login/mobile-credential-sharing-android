package uk.gov.onelogin.sharing.orchestration.prerequisites.contracts

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions.Companion.EXTRA_PERMISSIONS
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteAction

object PrerequisiteActionContract : ActivityResultContract<PrerequisiteAction, Unit>() {
    override fun createIntent(context: Context, input: PrerequisiteAction): Intent =
        Intent(input.intentAction).let { intent ->
            when (input) {
                is PrerequisiteAction.OpenAppPermissions ->
                    intent
                        .setData(
                            Uri.fromParts(
                                "package",
                                context.packageName,
                                null
                            )
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                is PrerequisiteAction.RequestPermissions ->
                    intent
                        .putExtra(EXTRA_PERMISSIONS, input.permissions.toTypedArray())

                else -> intent
            }
        }

    override fun parseResult(resultCode: Int, intent: Intent?) {
        // do nothing - handled within the result contract's 'onResult'
    }
}
