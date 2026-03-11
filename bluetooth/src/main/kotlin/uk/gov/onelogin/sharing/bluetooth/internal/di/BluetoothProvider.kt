package uk.gov.onelogin.sharing.bluetooth.internal.di

import android.bluetooth.BluetoothManager
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@BindingContainer
@ContributesTo(AppScope::class)
object BluetoothProvider {

    @Provides
    fun provideBluetoothManager(context: Context): BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)
}
