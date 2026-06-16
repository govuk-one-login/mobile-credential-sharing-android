package uk.gov.onelogin.sharing.testapp.di

import android.app.Activity
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.prerequisites.impl.permissions.ActivityPermissionChecker

@InstallIn(ActivityComponent::class)
@Module
object PermissionCheckerModule {

    @Provides
    @ActivityScoped
    fun providesPermissionChecker(
        @ActivityContext
        context: Context
    ): PermissionChecker = ActivityPermissionChecker(context as Activity)
}
