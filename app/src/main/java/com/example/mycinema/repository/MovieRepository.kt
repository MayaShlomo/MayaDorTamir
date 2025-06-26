package com.example.mycinema.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.mycinema.R
import com.example.mycinema.data.MovieDao
import com.example.mycinema.models.Movie
import kotlinx.coroutines.flow.Flow

class MovieRepository(private val dao: MovieDao) {
    val allMovies: LiveData<List<Movie>> = dao.getAllMovies()
    val favoriteMovies: LiveData<List<Movie>> = dao.getFavoriteMovies()
    val topRatedMovies: LiveData<List<Movie>> = dao.getTopRatedMovies()
    val allGenres: LiveData<List<String>> = dao.getAllGenres()

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

    // פונקציה לטעינת נתונים ראשונים - מעודכנת עם getString
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

            // נקה נתונים קיימים
            dao.clearAllMovies()

            // הוסף סרטים דוגמה
            val movieIds = mutableListOf<Long>()
            for (movie in sampleMovies) {
                val id = dao.insert(movie)
                movieIds.add(id)
                Log.d("MovieRepository", "Inserted movie: ${movie.title} with ID: $id")
            }

            // הגדר כמה סרטים כמועדפים
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

    // פונקציה מעודכנת עם שימוש ב-getString במקום hard-coded strings
    private fun getSampleMovies(context: Context): List<Movie> {
        return listOf(
            Movie(
                title = "Inception",
                description = context.getString(R.string.inception_description),
                genre = context.getString(R.string.genre_sci_fi),
                actors = "Leonardo DiCaprio, Joseph Gordon-Levitt, Ellen Page, Tom Hardy",
                director = "Christopher Nolan",
                year = 2010,
                rating = 8.8f,
                imageUri = "inception",
                showtime = "21:00",
                releaseDate = "2010-07-16",
                duration = 148,
                isFavorite = false
            ),
            Movie(
                title = "The Shawshank Redemption",
                description = context.getString(R.string.shawshank_description),
                genre = context.getString(R.string.genre_drama),
                actors = "Tim Robbins, Morgan Freeman, Bob Gunton",
                director = "Frank Darabont",
                year = 1994,
                rating = 9.3f,
                imageUri = "shawshank",
                showtime = "19:30",
                releaseDate = "1994-09-23",
                duration = 142,
                isFavorite = false
            ),
            Movie(
                title = "The Dark Knight",
                description = context.getString(R.string.dark_knight_description),
                genre = context.getString(R.string.genre_action),
                actors = "Christian Bale, Heath Ledger, Aaron Eckhart, Maggie Gyllenhaal",
                director = "Christopher Nolan",
                year = 2008,
                rating = 9.0f,
                imageUri = "dark_knight",
                showtime = "20:15",
                releaseDate = "2008-07-18",
                duration = 152,
                isFavorite = false
            ),
            Movie(
                title = "Pulp Fiction",
                description = "The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.",
                genre = context.getString(R.string.genre_crime),
                actors = "John Travolta, Uma Thurman, Samuel L. Jackson, Bruce Willis",
                director = "Quentin Tarantino",
                year = 1994,
                rating = 8.9f,
                imageUri = "pulp_fiction",
                showtime = "22:00",
                releaseDate = "1994-10-14",
                duration = 154,
                isFavorite = false
            ),
            Movie(
                title = "Parasite",
                description = "Greed and discrimination threaten the newly formed symbiotic relationship between the wealthy Park family and the destitute Kim clan.",
                genre = context.getString(R.string.genre_drama),
                actors = "Song Kang-ho, Lee Sun-kyun, Cho Yeo-jeong, Choi Woo-shik",
                director = "Bong Joon-ho",
                year = 2019,
                rating = 8.6f,
                imageUri = "parasite",
                showtime = "18:45",
                releaseDate = "2019-05-30",
                duration = 132,
                isFavorite = false
            ),
            Movie(
                title = "Interstellar",
                description = "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival as Earth becomes uninhabitable.",
                genre = context.getString(R.string.genre_sci_fi),
                actors = "Matthew McConaughey, Anne Hathaway, Jessica Chastain",
                director = "Christopher Nolan",
                year = 2014,
                rating = 8.6f,
                imageUri = "interstellar",
                showtime = "20:30",
                releaseDate = "2014-11-07",
                duration = 169,
                isFavorite = false
            ),
            Movie(
                title = "The Lion King",
                description = "Lion prince Simba grows up in the African heartland until tragedy forces him to run away. He ultimately learns to take his rightful place in the animal kingdom.",
                genre = context.getString(R.string.genre_animation),
                actors = "Donald Glover, Seth Rogen, Chiwetel Ejiofor, Alfre Woodard",
                director = "Jon Favreau",
                year = 2019,
                rating = 6.9f,
                imageUri = "lion_king",
                showtime = "17:15",
                releaseDate = "2019-07-19",
                duration = 118,
                isFavorite = false
            ),
            Movie(
                title = "The Godfather",
                description = "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.",
                genre = context.getString(R.string.genre_crime),
                actors = "Marlon Brando, Al Pacino, James Caan",
                director = "Francis Ford Coppola",
                year = 1972,
                rating = 9.2f,
                imageUri = "godfather",
                showtime = "21:30",
                releaseDate = "1972-03-24",
                duration = 175,
                isFavorite = false
            ),
            Movie(
                title = "Jurassic Park",
                description = "A pragmatic paleontologist touring an almost complete theme park on an island in Central America is tasked with protecting a couple of kids after a power failure causes the park's cloned dinosaurs to run loose.",
                genre = context.getString(R.string.genre_adventure),
                actors = "Sam Neill, Laura Dern, Jeff Goldblum",
                director = "Steven Spielberg",
                year = 1993,
                rating = 8.1f,
                imageUri = "jurassic_park",
                showtime = "16:45",
                releaseDate = "1993-06-11",
                duration = 127,
                isFavorite = false
            ),
            Movie(
                title = "Spirited Away",
                description = "During her family's move to the suburbs, a sullen 10-year-old girl wanders into a world ruled by gods, witches, and spirits, and where humans are changed into beasts.",
                genre = context.getString(R.string.genre_animation),
                actors = "Daveigh Chase, Suzanne Pleshette, Miyu Irino",
                director = "Hayao Miyazaki",
                year = 2001,
                rating = 8.6f,
                imageUri = "spirited_away",
                showtime = "18:00",
                releaseDate = "2001-07-20",
                duration = 125,
                isFavorite = false
            ),
            Movie(
                title = "Get Out",
                description = "A young African-American visits his white girlfriend's parents for the weekend, where his simmering uneasiness about their reception of him eventually reaches a boiling point.",
                genre = context.getString(R.string.genre_horror),
                actors = "Daniel Kaluuya, Allison Williams, Bradley Whitford",
                director = "Jordan Peele",
                year = 2017,
                rating = 7.7f,
                imageUri = "get_out",
                showtime = "22:30",
                releaseDate = "2017-02-24",
                duration = 104,
                isFavorite = false
            ),
            Movie(
                title = "La La Land",
                description = "While navigating their careers in Los Angeles, a pianist and an actress fall in love while attempting to reconcile their aspirations for the future.",
                genre = context.getString(R.string.genre_musical),
                actors = "Ryan Gosling, Emma Stone, John Legend",
                director = "Damien Chazelle",
                year = 2016,
                rating = 8.0f,
                imageUri = "la_la_land",
                showtime = "19:00",
                releaseDate = "2016-12-09",
                duration = 128,
                isFavorite = false
            )
        )
    }
}