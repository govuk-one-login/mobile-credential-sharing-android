package uk.gov.onelogin.sharing.sdk.api.shared

import android.content.Context
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk
import uk.gov.onelogin.sharing.sdk.internal.shared.CredentialSharingSdkImpl

interface CredentialSharingSdk {
    /**
     * @deprecated The app graph is an internal implementation detail. Use
     * [presentCredentialSdk] and [verifyCredentialSdk] with `createSession()` instead.
     */
    @Deprecated(
        message = "The app graph is an internal implementation detail. " +
            "Use createSession() via presentCredentialSdk/verifyCredentialSdk instead."
    )
    val appGraph: CredentialSharingAppGraph

    val presentCredentialSdk: PresentCredentialSdk
    val verifyCredentialSdk: VerifyCredentialSdk

    companion object {
        /**
         * Creates a new [CredentialSharingSdk] instance.
         *
         * The consumer controls the lifetime of this instance. Cache it in whatever
         * scope is appropriate (Application, Activity, etc.).
         */
        fun create(
            applicationContext: Context,
            logger: Logger,
            permissionChecker: PermissionChecker
        ): CredentialSharingSdk = CredentialSharingSdkImpl(
            applicationContext = applicationContext,
            logger = logger,
            permissionChecker = permissionChecker
        )
    }
}
