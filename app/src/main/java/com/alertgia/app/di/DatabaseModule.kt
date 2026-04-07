package com.alertgia.app.di

import android.content.Context
import androidx.room.Room
import com.alertgia.app.data.local.AlertgiaDatabase
import com.alertgia.app.data.local.dao.AllergyDao
import com.alertgia.app.data.local.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AlertgiaDatabase {
        return Room.databaseBuilder(
            context,
            AlertgiaDatabase::class.java,
            "alertgia_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserProfileDao(database: AlertgiaDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    fun provideAllergyDao(database: AlertgiaDatabase): AllergyDao {
        return database.allergyDao()
    }
}
