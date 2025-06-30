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

// מחלקה לנתוני הטופס
data class MovieFormData(
    val imageUri: String? = null,
    val selectedGenre: String? = null,
    val selectedYear: Int? = null,
    val selectedDate: String? = null,
    val selectedRating: Float? = null
)

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

    // *** נתוני טופס - חדש ***
    private val _movieFormData = MutableLiveData<MovieFormData>()
    val movieFormData: LiveData<MovieFormData> = _movieFormData

    init {
        Log.d("MovieViewModel", "ViewModel initialized with Hilt")
        _isOnlineSearchActive.value = false
        _movieFormData.value = MovieFormData() // אתחול נתוני הטופס
        preloadDataIfNeeded()
        loadPopularMovies()
    }

    // *** פונקציות נתוני טופס ***

    fun setImageUri(uri: String?) {
        _movieFormData.value = _movieFormData.value?.copy(imageUri = uri)
    }

    fun setSelectedGenre(genre: String?) {
        _movieFormData.value = _movieFormData.value?.copy(selectedGenre = genre)
    }

    fun setSelectedYear(year: Int?) {
        _movieFormData.value = _movieFormData.value?.copy(selectedYear = year)
    }

    fun setSelectedDate(date: String?) {
        _movieFormData.value = _movieFormData.value?.copy(selectedDate = date)
    }

    fun setSelectedRating(rating: Float?) {
        _movieFormData.value = _movieFormData.value?.copy(selectedRating = rating)
    }

    fun getMovieFormData(): MovieFormData {
        return _movieFormData.value ?: MovieFormData()
    }

    fun clearMovieFormData() {
        _movieFormData.value = MovieFormData()
    }
    fun getMovieById(movieId: Int): LiveData<Movie?> {
        return repository.getMovieById(movieId)
    }
    // *** פונקציות API קיימות ***

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

    fun loadPopularMovies() {
        Log.d("MovieViewModel", "Loading popular movies")
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            repository.getPopularMovies()
                .onSuccess { movies ->
                    Log.d("MovieViewModel", "Loaded ${movies.size} popular movies")
                    _popularMovies.value = movies
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

    fun addApiMovieToLocal(apiMovie: ApiMovie) {
        Log.d("MovieViewModel", "Adding API movie to local: ${apiMovie.title}")

        viewModelScope.launch {
            repository.addApiMovieToLocal(apiMovie)
                .onSuccess { id ->
                    Log.d("MovieViewModel", "Successfully added movie with ID: $id")
                }
                .onFailure { exception ->
                    Log.e("MovieViewModel", "Error adding movie: ${exception.message}")
                    _error.value = "Failed to add movie: ${exception.message}"
                }
        }
    }

    fun clearOnlineSearch() {
        _isOnlineSearchActive.value = false
        _onlineMovies.value = _popularMovies.value ?: emptyList()
        _error.value = null
    }

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

    fun clearError() {
        _error.value = null
    }

    fun clearAllMovies() = viewModelScope.launch {
        repository.clearAllMovies()
        Log.d("MovieViewModel", "All movies cleared from database")
    }

    private fun preloadDataIfNeeded() = viewModelScope.launch {
        try {
            Log.d("MovieViewModel", "Checking if sample data preload is needed")
        } catch (e: Exception) {
            Log.e("MovieViewModel", "Error during preload check: ${e.message}", e)
        }
    }
}