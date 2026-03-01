package com.ho.holive.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ho.holive.data.local.dao.LiveRoomDao
import com.ho.holive.data.local.entity.LiveRoomEntity

@Database(
    entities = [LiveRoomEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class HoliveDatabase : RoomDatabase() {
    abstract fun liveRoomDao(): LiveRoomDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE live_rooms ADD COLUMN viewerCount INTEGER NOT NULL DEFAULT 0",
                )
            }
        }
    }
}
