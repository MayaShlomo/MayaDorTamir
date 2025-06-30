// MovieRepository.kt - תיקון שימוש ב-BuildConfig
package com.example.mycinema.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.mycinema.BuildConfig
import com.example.mycinema.R
import com.example.mycinema.data.MovieDao
import com.example.mycinema.models.ApiMovie
import com.example.mycinema.models.Movie
import com.example.mycinema.models.MovieDetails
import com.example.mycinema.network.MovieApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val dao: MovieDao,
    private val apiService: MovieApiService,
    @ApplicationContext private val context: Context
) {

    companion object {
        // שימוש ב-BuildConfig במקום ערך קבוע
        private val TMDB_API_KEY = BuildConfig.TMDB_API_KEY
    }

    // פונקציות מקומיות קיימות
    val allMovies: LiveData<List<Movie>> = dao.getAllMovies()
    val favoriteMovies: LiveData<List<Movie>> = dao.getFavoriteMovies()
    val topRatedMovies: LiveData<List<Movie>> = dao.getTopRatedMovies()
    val allGenres: LiveData<List<String>> = dao.getAllGenres()
    fun getMovieById(movieId: Int): LiveData<Movie?> {
        return dao.getMovieById(movieId)
    }
    fun getAllMoviesFlow(): Flow<List<Movie>> = dao.getAllMoviesFlow()
    fun get(id: Int): LiveData<Movie> = dao.getById(id)
    suspend fun getByIdSync(id: Int): Movie? = dao.getByIdSync(id)

    fun searchMoviesByTitle(query: String): LiveData<List<Movie>> = dao.searchMoviesByTitle(query)
    fun getMoviesByGenre(genre: String): LiveData<List<Movie>> = dao.getMoviesByGenre(genre)
    suspend fun getFavoriteMoviesSync(): List<Movie> = dao.getFavoriteMoviesSync()

    suspend fun insert(movie: Movie): Long = dao.insert(movie)
    suspend fun update(movie: Movie) = dao.update(movie)
    suspend fun delete(movie: Movie) = dao.delete(movie)
    suspend fun clearAllMovies() = dao.clearAllMovies()

    // *** פונקציות API ***

    /**
     * חיפוש סרטים ברשת
     */
    suspend fun searchMoviesOnline(query: String): Result<List<ApiMovie>> {
        return try {
            Log.d("MovieRepository", "Searching online for: $query")

            // בדיקה שיש API Key
            if (TMDB_API_KEY.isEmpty() || TMDB_API_KEY == "YOUR_API_KEY_HERE") {
                Log.e("MovieRepository", "TMDb API Key not configured")
                return Result.failure(Exception("TMDb API Key not configured"))
            }

            val response = apiService.searchMovies(
                apiKey = TMDB_API_KEY,
                query = query
            )

            if (response.isSuccessful) {
                val movieResponse = response.body()
                if (movieResponse != null) {
                    Log.d("MovieRepository", "Found ${movieResponse.results.size} movies")
                    Result.success(movieResponse.results)
                } else {
                    Log.e("MovieRepository", "Response body is null")
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                Log.e("MovieRepository", "API Error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("API Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Network error: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * קבלת סרטים פופולריים
     */
    suspend fun getPopularMovies(): Result<List<ApiMovie>> {
        return try {
            Log.d("MovieRepository", "Fetching popular movies")

            if (TMDB_API_KEY.isEmpty() || TMDB_API_KEY == "YOUR_API_KEY_HERE") {
                Log.e("MovieRepository", "TMDb API Key not configured")
                return Result.failure(Exception("TMDb API Key not configured"))
            }

            val response = apiService.getPopularMovies(apiKey = TMDB_API_KEY)

            if (response.isSuccessful) {
                val movieResponse = response.body()
                if (movieResponse != null) {
                    Log.d("MovieRepository", "Found ${movieResponse.results.size} popular movies")
                    Result.success(movieResponse.results)
                } else {
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                Log.e("MovieRepository", "API Error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error fetching popular movies: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * קבלת פרטי סרט ספציפי
     */
    suspend fun getMovieDetailsFromApi(movieId: Int): Result<MovieDetails> {
        return try {
            Log.d("MovieRepository", "Fetching details for movie: $movieId")

            if (TMDB_API_KEY.isEmpty() || TMDB_API_KEY == "YOUR_API_KEY_HERE") {
                return Result.failure(Exception("TMDb API Key not configured"))
            }

            val response = apiService.getMovieDetails(
                movieId = movieId,
                apiKey = TMDB_API_KEY
            )

            if (response.isSuccessful) {
                val movieDetails = response.body()
                if (movieDetails != null) {
                    Log.d("MovieRepository", "Got details for: ${movieDetails.title}")
                    Result.success(movieDetails)
                } else {
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error fetching movie details: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * קבלת סרטים מומלצים
     */
    suspend fun getTopRatedMoviesFromApi(): Result<List<ApiMovie>> {
        return try {
            if (TMDB_API_KEY.isEmpty() || TMDB_API_KEY == "YOUR_API_KEY_HERE") {
                return Result.failure(Exception("TMDb API Key not configured"))
            }

            val response = apiService.getTopRatedMovies(apiKey = TMDB_API_KEY)

            if (response.isSuccessful) {
                val movieResponse = response.body()
                if (movieResponse != null) {
                    Result.success(movieResponse.results)
                } else {
                    Result.failure(Exception("Empty response"))
                }
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error fetching top rated movies: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * המרה מ-ApiMovie ל-Movie מקומי
     */
    fun apiMovieToMovie(apiMovie: ApiMovie): Movie {
        return Movie(
            id = 0, // Room יגדיר ID חדש
            apiId = apiMovie.id,
            title = apiMovie.title,
            description = apiMovie.overview,
            genre = "", // ריק כי הסרנו את GenreMapper
            actors = null, // לא זמין ב-API הבסיסי
            director = null, // לא זמין ב-API הבסיסי
            year = apiMovie.releaseDate?.substring(0, 4)?.toIntOrNull(),
            rating = apiMovie.voteAverage,
            imageUri = MovieApiService.getPosterUrl(apiMovie.posterPath),
            showtime = "20:00", // ברירת מחדל
            isFavorite = false,
            releaseDate = apiMovie.releaseDate,
            duration = null, // לא זמין ב-API הבסיסי
            isFromApi = true
        )
    }

    /**
     * הוספת סרט מ-API למקומי
     */
    suspend fun addApiMovieToLocal(apiMovie: ApiMovie): Result<Long> {
        return try {
            val localMovie = apiMovieToMovie(apiMovie)
            val id = dao.insert(localMovie)
            Log.d("MovieRepository", "Added API movie to local DB with ID: $id")
            Result.success(id)
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error adding API movie to local: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * בדיקה אם סרט כבר קיים במאגר המקומי
     */
    suspend fun isMovieInLocal(apiMovieId: Int): Boolean {
        return try {
            // בינתיים מחזיר false, אפשר לפתח בעתיד
            false
        } catch (e: Exception) {
            false
        }
    }

    // פונקציות קיימות לטעינת נתונים ראשוניים
    suspend fun preloadSampleMoviesIfNeeded(context: Context): Boolean {
        return try {
            Log.d("MovieRepository", "Checking if preload is needed")

            val existingMovies = dao.getFavoriteMoviesSync()
            if (existingMovies.isNotEmpty()) {
                Log.d("MovieRepository", "Database already has movies, skipping preload")
                return false
            }

            Log.d("MovieRepository", "Starting sample data preload")
            val sampleMovies = getSampleMovies(context)

            dao.clearAllMovies()

            val movieIds = mutableListOf<Long>()
            for (movie in sampleMovies) {
                val id = dao.insert(movie)
                movieIds.add(id)
                Log.d("MovieRepository", "Inserted movie: ${movie.title} with ID: $id")
            }

            if (movieIds.size >= 2) {
                val movie1 = dao.getByIdSync(movieIds[0].toInt())
                if (movie1 != null) {
                    dao.update(movie1.copy(isFavorite = true))
                    Log.d("MovieRepository", "Set ${movie1.title} as favorite")
                }

                val movie2 = dao.getByIdSync(movieIds[1].toInt())
                if (movie2 != null) {
                    dao.update(movie2.copy(isFavorite = true))
                    Log.d("MovieRepository", "Set ${movie2.title} as favorite")
                }
            }

            Log.d("MovieRepository", "Preload completed successfully. Inserted ${movieIds.size} movies")
            true
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error during preload: ${e.message}", e)
            false
        }
    }

    private fun getSampleMovies(context: Context): List<Movie> {
        return listOf(
            Movie(
                title = context.getString(R.string.sample_movie_inception_title),
                description = context.getString(R.string.inception_description),
                genre = context.getString(R.string.genre_sci_fi),
                actors = context.getString(R.string.sample_inception_actors),
                director = context.getString(R.string.sample_inception_director),
                year = 2010,
                rating = 8.8f,
                imageUri = "inception",
                showtime = "21:00",
                releaseDate = "2010-07-16",
                duration = 148,
                isFavorite = false
            ),
            Movie(
                title = context.getString(R.string.sample_movie_shawshank_title),
                description = context.getString(R.string.shawshank_description),
                genre = context.getString(R.string.genre_drama),
                actors = context.getString(R.string.sample_shawshank_actors),
                director = context.getString(R.string.sample_shawshank_director),
                year = 1994,
                rating = 9.3f,
                imageUri = "shawshank",
                showtime = "19:30",
                releaseDate = "1994-09-23",
                duration = 142,
                isFavorite = false
            ),
//            Movie(
//                title = context.getString(R.string.sample_movie_dark_knight_title),
//                description = context.getString(R.string.dark_knight_description),
//                genre = context.getString(R.string.genre_action),
//                actors = context.getString(R.string.sample_dark_knight_actors),
//                director = context.getString(R.string.sample_dark_knight_director),
//                year = 2008,
//                rating = 9.0f,
//                imageUri = "dark_knight",
//                showtime = "20:15",
//                releaseDate = "2008-07-18",
//                duration = 152,
//                isFavorite = false
//            )
        )
    }
}