package uk.gov.onelogin.sharing.testapp.di

import android.app.Activity
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import uk.gov.onelogin.sharing.core.permission.ActivityPermissionChecker
import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2

@InstallIn(ActivityComponent::class)
@Module
object PermissionCheckerModule {

    @Provides
    fun providesPermissionChecker(
        @ActivityContext
        context: Context
    ): PermissionCheckerV2 = ActivityPermissionChecker(context as Activity)
}
