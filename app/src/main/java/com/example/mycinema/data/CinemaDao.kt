// CinemaDao.kt
package com.example.mycinema.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mycinema.models.Cinema
import com.example.mycinema.models.Showtime

@Dao
interface CinemaDao {

    // Cinema queries
    @Query("SELECT * FROM cinemas WHERE isOpen = 1")
    suspend fun getAllActiveCinemas(): List<Cinema>

    @Query("SELECT * FROM cinemas WHERE id = :id")
    suspend fun getCinemaById(id: Int): Cinema?

    @Query("SELECT * FROM cinemas WHERE id = :id")
    fun getCinemaByIdLiveData(id: Int): LiveData<Cinema>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCinemas(cinemas: List<Cinema>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCinema(cinema: Cinema): Long

    @Update
    suspend fun updateCinema(cinema: Cinema)

    // Showtime queries
    @Query("SELECT * FROM showtimes WHERE cinemaId = :cinemaId AND showDate = :date ORDER BY showTime")
    suspend fun getShowtimesByCinemaAndDate(cinemaId: Int, date: String): List<Showtime>

    @Query("SELECT * FROM showtimes WHERE cinemaId = :cinemaId AND showDate >= :fromDate ORDER BY showDate, showTime")
    suspend fun getUpcomingShowtimes(cinemaId: Int, fromDate: String): List<Showtime>

    @Query("SELECT * FROM showtimes WHERE movieTitle LIKE '%' || :movieTitle || '%' AND showDate >= :fromDate")
    suspend fun getShowtimesByMovie(movieTitle: String, fromDate: String): List<Showtime>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShowtimes(showtimes: List<Showtime>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShowtime(showtime: Showtime): Long

    @Delete
    suspend fun deleteShowtime(showtime: Showtime)

    // Complex queries
    @Query("""
        SELECT c.* FROM cinemas c 
        INNER JOIN showtimes s ON c.id = s.cinemaId 
        WHERE s.movieTitle LIKE '%' || :movieTitle || '%' 
        AND s.showDate >= :fromDate 
        AND c.isOpen = 1
        GROUP BY c.id
    """)
    suspend fun getCinemasShowingMovie(movieTitle: String, fromDate: String): List<Cinema>

    @Query("DELETE FROM showtimes WHERE showDate < :date")
    suspend fun cleanOldShowtimes(date: String)

    @Query("SELECT COUNT(*) FROM cinemas WHERE isOpen = 1")
    suspend fun getActiveCinemasCount(): Int
}
