package com.example.mycinema.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.mycinema.models.ApiMovie
import com.example.mycinema.models.Movie
import com.example.mycinema.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    // LiveData מקומי קיים
    val allMovies = repository.allMovies
    val favoriteMovies = repository.favoriteMovies
    val topRatedMovies = repository.topRatedMovies
    val allGenres = repository.allGenres

    // LiveData למסננים וחיפושים מקומיים
    private val _searchResults = MutableLiveData<List<Movie>>()
    val searchResults: LiveData<List<Movie>> = _searchResults

    private val _currentFilter = MutableLiveData<String>()
    val currentFilter: LiveData<String> = _currentFilter

    // *** LiveData חדש לAPI ***

    // נתוני API
    private val _onlineMovies = MutableLiveData<List<ApiMovie>>()
    val onlineMovies: LiveData<List<ApiMovie>> = _onlineMovies

    private val _popularMovies = MutableLiveData<List<ApiMovie>>()
    val popularMovies: LiveData<List<ApiMovie>> = _popularMovies

    // מצבי טעינה ושגיאות
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isOnlineSearchActive = MutableLiveData<Boolean>()
    val isOnlineSearchActive: LiveData<Boolean> = _isOnlineSearchActive

    // משתנים לטיפול בטפסים (קיימים)
    private val _imageUri = MutableLiveData<String?>()
    private val _releaseDate = MutableLiveData<String?>()
    private val _rating = MutableLiveData<Float?>()
    private val _year = MutableLiveData<Int?>()

    init {
        Log.d("MovieViewModel", "ViewModel initialized with Hilt")
        _isOnlineSearchActive.value = false
        preloadDataIfNeeded()

        // טען סרטים פופולריים בהפעלה ראשונה
        loadPopularMovies()
    }

    // *** פונקציות API חדשות ***

    /**
     * חיפוש סרטים ברשת
     */
    fun searchMoviesOnline(query: String) {
        if (query.isBlank()) {
            _onlineMovies.value = emptyList()
            return
        }

        Log.d("MovieViewModel", "Searching online for: $query")
        _loading.value = true
        _error.value = null
        _isOnlineSearchActive.value = true

        viewModelScope.launch {
            repository.searchMoviesOnline(query)
                .onSuccess { movies ->
                    Log.d("MovieViewModel", "Found ${movies.size} movies online")
                    _onlineMovies.value = movies
                    _error.value = null
                }
                .onFailure { exception ->
                    Log.e("MovieViewModel", "Error searching online: ${exception.message}")
                    _error.value = exception.message
                    _onlineMovies.value = emptyList()
                }

            _loading.value = false
        }
    }

    /**
     * טעינת סרטים פופולריים
     */
    fun loadPopularMovies() {
        Log.d("MovieViewModel", "Loading popular movies")
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            repository.getPopularMovies()
                .onSuccess { movies ->
                    Log.d("MovieViewModel", "Loaded ${movies.size} popular movies")
                    _popularMovies.value = movies
                    // אם אין חיפוש פעיל, תציג סרטים פופולריים
                    if (_isOnlineSearchActive.value == false) {
                        _onlineMovies.value = movies
                    }
                    _error.value = null
                }
                .onFailure { exception ->
                    Log.e("MovieViewModel", "Error loading popular movies: ${exception.message}")
                    _error.value = exception.message
                }

            _loading.value = false
        }
    }

    /**
     * הוספת סרט מ-API למקומי
     */
    fun addApiMovieToLocal(apiMovie: ApiMovie) {
        Log.d("MovieViewModel", "Adding API movie to local: ${apiMovie.title}")

        viewModelScope.launch {
            repository.addApiMovieToLocal(apiMovie)
                .onSuccess { id ->
                    Log.d("MovieViewModel", "Successfully added movie with ID: $id")
                    // ניתן להוסיף Toast או הודעה למשתמש
                }
                .onFailure { exception ->
                    Log.e("MovieViewModel", "Error adding movie: ${exception.message}")
                    _error.value = "Failed to add movie: ${exception.message}"
                }
        }
    }

    /**
     * ניקוי חיפוש אונליין וחזרה לפופולריים
     */
    fun clearOnlineSearch() {
        _isOnlineSearchActive.value = false
        _onlineMovies.value = _popularMovies.value ?: emptyList()
        _error.value = null
    }

    /**
     * רענון סרטים פופולריים
     */
    fun refreshPopularMovies() {
        loadPopularMovies()
    }

    // *** פונקציות מקומיות קיימות ***

    fun getAllMoviesFlow(): Flow<List<Movie>> = repository.getAllMoviesFlow()
    fun get(id: Int) = repository.get(id)

    fun searchMovies(query: String) {
        _currentFilter.value = query
        viewModelScope.launch {
            repository.searchMoviesByTitle(query).observeForever { results ->
                _searchResults.value = results
            }
        }
    }

    fun filterByGenre(genre: String) {
        _currentFilter.value = genre
        viewModelScope.launch {
            repository.getMoviesByGenre(genre).observeForever { results ->
                _searchResults.value = results
            }
        }
    }

    fun clearFilters() {
        Log.d("MovieViewModel", "Clearing local filters")
        _currentFilter.value = ""
        _searchResults.value = allMovies.value

        viewModelScope.launch {
            delay(50)
            _searchResults.postValue(allMovies.value)
        }
    }

    fun insert(m: Movie) = viewModelScope.launch {
        val id = repository.insert(m)
        Log.d("MovieViewModel", "Inserted movie with ID: $id")
    }

    fun update(m: Movie) = viewModelScope.launch {
        Log.d("MovieViewModel", "Updating movie: ${m.id} - ${m.title} - isFavorite: ${m.isFavorite}")
        repository.update(m)
    }

    fun delete(m: Movie) = viewModelScope.launch {
        repository.delete(m)
    }

    fun refreshFavorites() {
        viewModelScope.launch {
            val currentFavorites = repository.getFavoriteMoviesSync()
            Log.d("MovieViewModel", "Refreshing favorites, found: ${currentFavorites.size}")
        }
    }

    fun toggleFavorite(m: Movie) {
        Log.d("MovieViewModel", "Toggling favorite for ${m.title}: ${m.isFavorite} -> ${!m.isFavorite}")
        val updatedMovie = m.copy(isFavorite = !m.isFavorite)

        viewModelScope.launch {
            try {
                repository.update(updatedMovie)
                Log.d("MovieViewModel", "Update successful for movie ID: ${updatedMovie.id}")
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error updating favorite status: ${e.message}", e)
            }
        }
    }

    // פונקציות טפסים קיימות
    fun setImageUri(uri: String) { _imageUri.value = uri }
    fun setReleaseDate(date: String) { _releaseDate.value = date }
    fun setRating(rating: Float) { _rating.value = rating }
    fun setYear(year: Int) { _year.value = year }
    fun getImageUri(): String? = _imageUri.value

    /**
     * ניקוי הודעת שגיאה
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * ניקוי כל הנתונים במאגר
     */
    fun clearAllMovies() = viewModelScope.launch {
        repository.clearAllMovies()
        Log.d("MovieViewModel", "All movies cleared from database")
    }

    private fun preloadDataIfNeeded() = viewModelScope.launch {
        try {
            Log.d("MovieViewModel", "Checking if sample data preload is needed")
            // הועבר לRepository עם DI
        } catch (e: Exception) {
            Log.e("MovieViewModel", "Error during preload check: ${e.message}", e)
        }
    }
}