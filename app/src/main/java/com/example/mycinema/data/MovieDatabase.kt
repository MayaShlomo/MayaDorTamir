package com.example.mycinema.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mycinema.models.Movie

@Database(entities = [Movie::class], version = 3, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile private var INSTANCE: MovieDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. הוספת עמודת director לשם הבמאי
                database.execSQL("ALTER TABLE movies ADD COLUMN director TEXT")

                // 2. הוספת עמודת year לשנת יציאה
                database.execSQL("ALTER TABLE movies ADD COLUMN year INTEGER")

                // 3. הוספת עמודת rating לציון הסרט (בפורמט REAL)
                database.execSQL("ALTER TABLE movies ADD COLUMN rating REAL")

                // 4. הוספת עמודת releaseDate לתאריך שחרור (טקסט)
                database.execSQL("ALTER TABLE movies ADD COLUMN releaseDate TEXT")

                // 5. הוספת עמודת duration לאורך הסרט בדקות
                database.execSQL("ALTER TABLE movies ADD COLUMN duration INTEGER")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // הוספת עמודות חדשות לתמיכה ב-API
                database.execSQL("ALTER TABLE movies ADD COLUMN apiId INTEGER")
                database.execSQL("ALTER TABLE movies ADD COLUMN isFromApi INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): MovieDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "movie_database"
                )
                    .fallbackToDestructiveMigration()
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}