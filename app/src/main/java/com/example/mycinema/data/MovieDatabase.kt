// MovieDatabase.kt - תיקון השגיאות
package com.example.mycinema.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mycinema.models.Cinema
import com.example.mycinema.models.Movie
import com.example.mycinema.models.Showtime

@Database(
    entities = [Movie::class, Cinema::class, Showtime::class],
    version = 5, // עליתי בגרסה כדי לטפל בבעיית ה-Foreign Key
    exportSchema = false
)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun cinemaDao(): CinemaDao

    companion object {
        @Volatile
        private var INSTANCE: MovieDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // הוספת עמודות לטבלת movies
                database.execSQL("ALTER TABLE movies ADD COLUMN director TEXT")
                database.execSQL("ALTER TABLE movies ADD COLUMN year INTEGER")
                database.execSQL("ALTER TABLE movies ADD COLUMN rating REAL")
                database.execSQL("ALTER TABLE movies ADD COLUMN releaseDate TEXT")
                database.execSQL("ALTER TABLE movies ADD COLUMN duration INTEGER")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // הוספת עמודות API לטבלת movies
                database.execSQL("ALTER TABLE movies ADD COLUMN apiId INTEGER")
                database.execSQL("ALTER TABLE movies ADD COLUMN isFromApi INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // יצירת טבלת בתי קולנוע
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS cinemas (
                        id INTEGER PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        address TEXT NOT NULL,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        phone TEXT,
                        website TEXT,
                        imageUrl TEXT,
                        rating REAL NOT NULL DEFAULT 0,
                        isOpen INTEGER NOT NULL DEFAULT 1
                    )
                """)

                // יצירת טבלת הקרנות
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS showtimes (
                        id INTEGER PRIMARY KEY NOT NULL,
                        cinemaId INTEGER NOT NULL,
                        movieTitle TEXT NOT NULL,
                        showDate TEXT NOT NULL,
                        showTime TEXT NOT NULL,
                        price REAL,
                        isAvailable INTEGER NOT NULL DEFAULT 1,
                        movieId INTEGER,
                        FOREIGN KEY(cinemaId) REFERENCES cinemas(id) ON DELETE CASCADE
                    )
                """)

                // יצירת אינדקסים לביצועים טובים יותר
                database.execSQL("CREATE INDEX IF NOT EXISTS index_showtimes_cinemaId ON showtimes(cinemaId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_showtimes_showDate ON showtimes(showDate)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_showtimes_movieTitle ON showtimes(movieTitle)")
            }
        }

        // Migration חדש מגרסה 4 ל-5 - תיקון בעיית ה-Schema
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // אין צורך בשינויים כי הטבלאות כבר קיימות עם המבנה הנכון
                // Migration זה נועד רק לעדכן את ה-schema version
                // בגלל שהוספנו Foreign Keys ו-Indices למודל
            }
        }

        fun getDatabase(context: Context): MovieDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MovieDatabase::class.java,
                    "movie_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration() // רק לפיתוח - זה ימחק נתונים אם יש בעיה
                    .build()
                    .also { INSTANCE = it }
            }
    }
}