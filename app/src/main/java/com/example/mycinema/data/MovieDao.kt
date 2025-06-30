package com.example.mycinema.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.mycinema.models.Movie
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: Movie): Long

    @Update
    suspend fun update(movie: Movie)

    @Delete
    suspend fun delete(movie: Movie)

    @Query("SELECT * FROM movies ORDER BY id DESC")
    fun getAllMovies(): LiveData<List<Movie>>

    @Query("SELECT * FROM movies WHERE isFavorite = 1")
    fun getFavoriteMovies(): LiveData<List<Movie>>

    @Query("SELECT * FROM movies WHERE isFavorite = 1")
    suspend fun getFavoriteMoviesSync(): List<Movie>

    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1")
    fun getById(id: Int): LiveData<Movie>

    // *** הוספת הפונקציה החסרה שהיית צריך ***
    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    fun getMovieById(movieId: Int): LiveData<Movie?>

    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1")
    suspend fun getByIdSync(id: Int): Movie?

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :searchQuery || '%'")
    fun searchMoviesByTitle(searchQuery: String): LiveData<List<Movie>>

    @Query("SELECT * FROM movies WHERE genre = :genre")
    fun getMoviesByGenre(genre: String): LiveData<List<Movie>>

    @Query("SELECT DISTINCT genre FROM movies WHERE genre IS NOT NULL")
    fun getAllGenres(): LiveData<List<String>>

    @Query("SELECT * FROM movies ORDER BY rating DESC LIMIT 10")
    fun getTopRatedMovies(): LiveData<List<Movie>>

    @Query("SELECT * FROM movies ORDER BY id DESC")
    fun getAllMoviesFlow(): Flow<List<Movie>>

    @Query("DELETE FROM movies")
    suspend fun clearAllMovies()
}