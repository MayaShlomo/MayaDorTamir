package com.example.mycinema.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mycinema.models.Movie

@Database(entities = [Movie::class], version = 2, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile private var INSTANCE: MovieDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. הוספת עמודת director לשם הבמאי
                val addDirector = "ALTER TABLE movies ADD COLUMN director TEXT"
                database.execSQL(addDirector)

                // 2. הוספת עמודת year לשנת יציאה
                val addYear = "ALTER TABLE movies ADD COLUMN year INTEGER"
                database.execSQL(addYear)

                // 3. הוספת עמודת rating לציון הסרט (בפורמט REAL)
                val addRating = "ALTER TABLE movies ADD COLUMN rating REAL"
                database.execSQL(addRating)

                // 4. הוספת עמודת releaseDate לתאריך שחרור (טקסט)
                val addReleaseDate = "ALTER TABLE movies ADD COLUMN releaseDate TEXT"
                database.execSQL(addReleaseDate)

                // 5. הוספת עמודת duration לאורך הסרט בדקות
                val addDuration = "ALTER TABLE movies ADD COLUMN duration INTEGER"
                database.execSQL(addDuration)
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
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}