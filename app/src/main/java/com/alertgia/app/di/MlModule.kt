package com.alertgia.app.di

import android.content.Context
import com.alertgia.app.data.ml.FoodClassifier
import com.alertgia.app.data.ml.ObjectDetector
import com.alertgia.app.util.ConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MlModule {

    @Provides
    @Singleton
    fun provideFoodClassifier(@ApplicationContext context: Context): FoodClassifier {
        return FoodClassifier(context).also { it.initialize() }
    }

    @Provides
    @Singleton
    fun provideObjectDetector(@ApplicationContext context: Context): ObjectDetector {
        return ObjectDetector(context).also { it.initialize() }
    }

    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
        return ConnectivityObserver(context)
    }
}
