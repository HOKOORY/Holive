package com.ho.holive.di

import android.content.Context
import androidx.room.Room
import com.ho.holive.data.local.HoliveDatabase
import com.ho.holive.data.local.dao.LiveRoomDao
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
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): HoliveDatabase {
        return Room.databaseBuilder(context, HoliveDatabase::class.java, "holive.db")
            .addMigrations(HoliveDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides
    fun provideLiveRoomDao(database: HoliveDatabase): LiveRoomDao = database.liveRoomDao()
}
