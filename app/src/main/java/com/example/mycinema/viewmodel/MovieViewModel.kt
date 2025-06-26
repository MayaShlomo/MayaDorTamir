package com.example.mycinema.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.mycinema.data.MovieDatabase
import com.example.mycinema.models.Movie
import com.example.mycinema.repository.MovieRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MovieViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = MovieRepository(MovieDatabase.getDatabase(app).movieDao())

    val allMovies = repo.allMovies
    val favoriteMovies = repo.favoriteMovies
    val topRatedMovies = repo.topRatedMovies
    val allGenres = repo.allGenres

    private val _searchResults = MutableLiveData<List<Movie>>()
    val searchResults: LiveData<List<Movie>> = _searchResults

    private val _currentFilter = MutableLiveData<String>()
    val currentFilter: LiveData<String> = _currentFilter

    // משתנים חדשים לטיפול בנתוני הטופס במקום לשמור ב-Fragment
    private val _imageUri = MutableLiveData<String?>()
    private val _releaseDate = MutableLiveData<String?>()
    private val _rating = MutableLiveData<Float?>()
    private val _year = MutableLiveData<Int?>()

    init {
        Log.d("MovieViewModel", "ViewModel initialized")
        preloadDataIfNeeded()
    }

    // פונקציות לטיפול בנתוני הטופס
    fun setImageUri(uri: String) { _imageUri.value = uri }
    fun setReleaseDate(date: String) { _releaseDate.value = date }
    fun setRating(rating: Float) { _rating.value = rating }
    fun setYear(year: Int) { _year.value = year }

    fun getImageUri(): String? = _imageUri.value

    fun getAllMoviesFlow(): Flow<List<Movie>> = repo.getAllMoviesFlow()
    fun get(id: Int) = repo.get(id)

    fun searchMovies(query: String) {
        _currentFilter.value = query
        viewModelScope.launch {
            repo.searchMoviesByTitle(query).observeForever { results ->
                _searchResults.value = results
            }
        }
    }

    fun filterByGenre(genre: String) {
        _currentFilter.value = genre
        viewModelScope.launch {
            repo.getMoviesByGenre(genre).observeForever { results ->
                _searchResults.value = results
            }
        }
    }

    fun clearFilters() {
        Log.d("MovieViewModel", "Clearing filters")
        _currentFilter.value = ""
        _searchResults.value = allMovies.value

        viewModelScope.launch {
            delay(50)
            _searchResults.postValue(allMovies.value)
        }
    }

    fun insert(m: Movie) = viewModelScope.launch {
        // Room עובר ל-IO Thread באופן אוטומטי - לא צריך Dispatchers.IO
        val id = repo.insert(m)
        Log.d("MovieViewModel", "Inserted movie with ID: $id")
    }

    fun update(m: Movie) = viewModelScope.launch {
        Log.d("MovieViewModel", "Updating movie: ${m.id} - ${m.title} - isFavorite: ${m.isFavorite}")
        repo.update(m)
    }

    fun delete(m: Movie) = viewModelScope.launch {
        repo.delete(m)
    }

    fun refreshFavorites() {
        viewModelScope.launch {
            val currentFavorites = repo.getFavoriteMoviesSync()
            Log.d("MovieViewModel", "Refreshing favorites, found: ${currentFavorites.size}")
        }
    }

    fun toggleFavorite(m: Movie) {
        Log.d("MovieViewModel", "Toggling favorite for ${m.title}: ${m.isFavorite} -> ${!m.isFavorite}")
        val updatedMovie = m.copy(isFavorite = !m.isFavorite)

        viewModelScope.launch {
            try {
                repo.update(updatedMovie)
                Log.d("MovieViewModel", "Update successful for movie ID: ${updatedMovie.id}")
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error updating favorite status: ${e.message}", e)
            }
        }
    }

    // פונקציה פשוטה לטעינת נתונים ראשוניים
    private fun preloadDataIfNeeded() = viewModelScope.launch {
        try {
            Log.d("MovieViewModel", "Checking if sample data preload is needed")
            val success = repo.preloadSampleMoviesIfNeeded(getApplication())
            if (success) {
                Log.d("MovieViewModel", "Sample data preloaded successfully")
            } else {
                Log.d("MovieViewModel", "Sample data preload not needed or failed")
            }
        } catch (e: Exception) {
            Log.e("MovieViewModel", "Error during preload check: ${e.message}", e)
        }
    }
}