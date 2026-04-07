package com.alertgia.app.di

import android.content.Context
import com.alertgia.app.data.preferences.AppPreferences
import com.alertgia.app.util.TtsHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferences(context)
    }

    @Provides
    @Singleton
    fun provideTtsHelper(@ApplicationContext context: Context): TtsHelper {
        return TtsHelper(context).also { it.initialize() }
    }
}
