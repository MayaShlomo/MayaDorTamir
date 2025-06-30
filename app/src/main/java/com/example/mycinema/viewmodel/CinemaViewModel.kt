// CinemaViewModel.kt
// החלף את: app/src/main/java/com/example/mycinema/viewmodel/CinemaViewModel.kt
package com.example.mycinema.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycinema.models.*
import com.example.mycinema.repository.CinemaRepository
import com.example.mycinema.services.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CinemaViewModel @Inject constructor(
    private val cinemaRepository: CinemaRepository,
    private val locationService: LocationService
) : ViewModel() {

    // נתוני בתי קולנוע
    private val _nearbyCinemas = MutableLiveData<List<CinemaWithDistance>>()
    val nearbyCinemas: LiveData<List<CinemaWithDistance>> = _nearbyCinemas

    private val _selectedCinema = MutableLiveData<CinemaWithShowtimes?>()
    val selectedCinema: LiveData<CinemaWithShowtimes?> = _selectedCinema

    // מיקום משתמש
    private val _userLocation = MutableLiveData<UserLocation?>()
    val userLocation: LiveData<UserLocation?> = _userLocation

    // מצבי UI
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _hasLocationPermission = MutableLiveData<Boolean>()
    val hasLocationPermission: LiveData<Boolean> = _hasLocationPermission

    // סוג הנתונים הנוכחי
    private val _dataSourceType = MutableLiveData<DataSourceType>()
    val dataSourceType: LiveData<DataSourceType> = _dataSourceType

    enum class DataSourceType {
        SAMPLE_DATA,      // נתונים לדוגמה
        FAMOUS_CINEMAS,   // בתי קולנוע מפורסמים
        COMBINED,         // שילוב
        BY_COUNTRY        // לפי מדינה
    }

    init {
        Log.d("CinemaViewModel", "ViewModel initialized")
        checkLocationPermission()
        _dataSourceType.value = DataSourceType.FAMOUS_CINEMAS
        preloadSampleData()

        // *** טעינה מיידית של כל בתי הקולנוע המפורסמים ***
        loadAllFamousCinemas()
    }

    // ================================
    // פונקציות חדשות - בתי קולנוע מפורסמים
    // ================================

    /**
     * טעינת בתי קולנוע מפורסמים בקרבת מקום - תיקון
     */
    fun loadFamousCinemas(radiusKm: Double = 100.0) {
        Log.d("CinemaViewModel", "Loading famous cinemas with radius: ${radiusKm}km")
        _loading.value = true
        _error.value = null
        _dataSourceType.value = DataSourceType.FAMOUS_CINEMAS

        viewModelScope.launch {
            try {
                val nearbyFamousCinemas = cinemaRepository.getFamousCinemasNearby(radiusKm)

                if (nearbyFamousCinemas.isEmpty()) {
                    // אם אין בקרבת מקום, טען כל בתי הקולנוע המפורסמים
                    Log.d("CinemaViewModel", "No famous cinemas in radius, loading all famous cinemas")
                    val allFamousCinemas = cinemaRepository.getAllFamousCinemas()
                    _nearbyCinemas.value = allFamousCinemas
                    Log.d("CinemaViewModel", "Loaded ${allFamousCinemas.size} famous cinemas worldwide")
                } else {
                    _nearbyCinemas.value = nearbyFamousCinemas
                    Log.d("CinemaViewModel", "Loaded ${nearbyFamousCinemas.size} famous cinemas nearby")
                }
            } catch (e: Exception) {
                Log.e("CinemaViewModel", "Error loading famous cinemas", e)
                _error.value = "Failed to load famous cinemas: ${e.message}"
                _nearbyCinemas.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * טעינת כל בתי הקולנוע המפורסמים
     */
    fun loadAllFamousCinemas() {
        Log.d("CinemaViewModel", "Loading all famous cinemas worldwide")
        _loading.value = true
        _error.value = null
        _dataSourceType.value = DataSourceType.FAMOUS_CINEMAS

        viewModelScope.launch {
            try {
                val cinemas = cinemaRepository.getAllFamousCinemas()
                _nearbyCinemas.value = cinemas
                Log.d("CinemaViewModel", "Loaded ${cinemas.size} famous cinemas worldwide")
            } catch (e: Exception) {
                Log.e("CinemaViewModel", "Error loading all famous cinemas", e)
                _error.value = "Failed to load cinemas: ${e.message}"
                _nearbyCinemas.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * טעינת בתי קולנוע לפי מדינה
     */
    fun loadCinemasByCountry(country: String) {
        Log.d("CinemaViewModel", "Loading cinemas for country: $country")
        _loading.value = true
        _error.value = null
        _dataSourceType.value = DataSourceType.BY_COUNTRY

        viewModelScope.launch {
            try {
                val cinemas = cinemaRepository.getCinemasByCountry(country)
                _nearbyCinemas.value = cinemas
                Log.d("CinemaViewModel", "Loaded ${cinemas.size} cinemas for $country")
            } catch (e: Exception) {
                Log.e("CinemaViewModel", "Error loading cinemas for $country", e)
                _error.value = "Failed to load cinemas for $country: ${e.message}"
                _nearbyCinemas.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * חיפוש בתי קולנוע
     */
    fun searchCinemas(query: String) {
        if (query.isBlank()) {
            loadFamousCinemas()
            return
        }

        Log.d("CinemaViewModel", "Searching cinemas for: $query")
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val cinemas = cinemaRepository.searchFamousCinemas(query)
                _nearbyCinemas.value = cinemas
                Log.d("CinemaViewModel", "Found ${cinemas.size} cinemas for query: $query")
            } catch (e: Exception) {
                Log.e("CinemaViewModel", "Error searching cinemas", e)
                _error.value = "Search failed: ${e.message}"
                _nearbyCinemas.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * טעינת נתונים משולבים
     */
    fun loadCombinedCinemas(radiusKm: Double = 50.0) {
        Log.d("CinemaViewModel", "Loading combined cinemas")
        _loading.value = true
        _error.value = null
        _dataSourceType.value = DataSourceType.COMBINED

        viewModelScope.launch {
            try {
                val cinemas = cinemaRepository.getCombinedCinemas(radiusKm)
                _nearbyCinemas.value = cinemas
                Log.d("CinemaViewModel", "Loaded ${cinemas.size} combined cinemas")
            } catch (e: Exception) {
                Log.e("CinemaViewModel", "Error loading combined cinemas", e)
                _error.value = "Failed to load cinemas: ${e.message}"
                _nearbyCinemas.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * טעינת פרטי בית קולנוע (עם או ללא הקרנות)
     */
    fun loadCinemaDetails(cinemaId: Int) {
        Log.d("CinemaViewModel", "Loading details for cinema: $cinemaId")
        _loading.value = true

        viewModelScope.launch {
            try {
                val cinemaWithShowtimes = cinemaRepository.getCinemaWithShowtimes(cinemaId)
                _selectedCinema.value = cinemaWithShowtimes

                if (cinemaWithShowtimes != null) {
                    Log.d("CinemaViewModel", "Loaded cinema: ${cinemaWithShowtimes.cinema.name} with ${cinemaWithShowtimes.showtimes.size} showtimes")
                } else {
                    Log.w("CinemaViewModel", "Cinema not found: $cinemaId")
                    _error.value = "Cinema not found"
                }
            } catch (e: Exception) {
                Log.e("CinemaViewModel", "Error loading cinema details", e)
                _error.value = "Failed to load cinema details: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // ================================
    // פונקציות קיימות - נתונים מקומיים (מתוקנות)
    // ================================

    /**
     * טעינת בתי קולנוע מקומיים בקרבת מקום (עם fallback למפורסמים)
     * *** תיקון עיקרי - תמיד טען משהו! ***
     */
    fun loadNearbyCinemas(radiusKm: Double = 15.0) {
        Log.d("CinemaViewModel", "Loading nearby cinemas with radius: ${radiusKm}km")
        _loading.value = true
        _error.value = null
        _dataSourceType.value = DataSourceType.SAMPLE_DATA

        viewModelScope.launch {
            try {
                val localCinemas = cinemaRepository.getCinemasNearby(radiusKm)

                if (localCinemas.isEmpty()) {
                    // אם אין בתי קולנוע מקומיים, טען מפורסמים עם רדיוס גדול יותר
                    Log.d("CinemaViewModel", "No local cinemas found, loading ALL famous cinemas")
                    val famousCinemas = cinemaRepository.getAllFamousCinemas() // *** שינוי מרכזי ***
                    _nearbyCinemas.value = famousCinemas
                    _dataSourceType.value = DataSourceType.FAMOUS_CINEMAS
                    Log.d("CinemaViewModel", "Loaded ${famousCinemas.size} famous cinemas instead")
                } else {
                    _nearbyCinemas.value = localCinemas
                    Log.d("CinemaViewModel", "Loaded ${localCinemas.size} local cinemas")
                }
            } catch (e: Exception) {
                Log.e("CinemaViewModel", "Error loading cinemas", e)
                _error.value = "Failed to load cinemas: ${e.message}"
                _nearbyCinemas.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * פונקציה חדשה - אולטרא fallback
     */
    fun loadAnyCinemas() {
        Log.d("CinemaViewModel", "Loading any available cinemas (ultra fallback)")
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // נסה הכל
                val localCinemas = cinemaRepository.getCinemasNearby(50000.0) // 50,000 ק"מ!
                val famousCinemas = cinemaRepository.getAllFamousCinemas()
                val combinedCinemas = cinemaRepository.getCombinedCinemas(50000.0)

                val bestOption = when {
                    localCinemas.isNotEmpty() -> {
                        _dataSourceType.value = DataSourceType.SAMPLE_DATA
                        localCinemas
                    }
                    famousCinemas.isNotEmpty() -> {
                        _dataSourceType.value = DataSourceType.FAMOUS_CINEMAS
                        famousCinemas
                    }
                    combinedCinemas.isNotEmpty() -> {
                        _dataSourceType.value = DataSourceType.COMBINED
                        combinedCinemas
                    }
                    else -> emptyList()
                }

                _nearbyCinemas.value = bestOption
                Log.d("CinemaViewModel", "Ultra fallback loaded ${bestOption.size} cinemas")

            } catch (e: Exception) {
                Log.e("CinemaViewModel", "Error in ultra fallback", e)
                _error.value = "Failed to load any cinemas: ${e.message}"
                _nearbyCinemas.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * חיפוש בתי קולנוע שמקרינים סרט מסוים
     */
    fun findCinemasShowingMovie(movieTitle: String) {
        Log.d("CinemaViewModel", "Finding cinemas showing: $movieTitle")
        _loading.value = true

        viewModelScope.launch {
            try {
                val cinemas = cinemaRepository.getCinemasShowingMovie(movieTitle)
                _nearbyCinemas.value = cinemas
                Log.d("CinemaViewModel", "Found ${cinemas.size} cinemas showing $movieTitle")
            } catch (e: Exception) {
                Log.e("CinemaViewModel", "Error finding cinemas for movie", e)
                _error.value = "Failed to find cinemas: ${e.message}"
                _nearbyCinemas.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    // ================================
    // פונקציות כלליות
    // ================================

    /**
     * עדכון מיקום משתמש
     */
    fun updateUserLocation() {
        if (!locationService.hasLocationPermission()) {
            _hasLocationPermission.value = false
            return
        }

        viewModelScope.launch {
            try {
                val location = locationService.getCurrentLocation()
                location?.let {
                    val userLoc = UserLocation(it.latitude, it.longitude)
                    _userLocation.value = userLoc
                    Log.d("CinemaViewModel", "Updated user location: ${it.latitude}, ${it.longitude}")
                }
            } catch (e: Exception) {
                Log.e("CinemaViewModel", "Error getting user location", e)
                _error.value = "Failed to get location: ${e.message}"
            }
        }
    }

    /**
     * בדיקת הרשאות מיקום
     */
    fun checkLocationPermission() {
        _hasLocationPermission.value = locationService.hasLocationPermission()
    }

    /**
     * ניקוי שגיאות
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * רענון נתונים
     */
    fun refresh() {
        when (_dataSourceType.value) {
            DataSourceType.FAMOUS_CINEMAS -> loadFamousCinemas()
            DataSourceType.SAMPLE_DATA -> loadNearbyCinemas()
            DataSourceType.COMBINED -> loadCombinedCinemas()
            DataSourceType.BY_COUNTRY -> loadAllFamousCinemas()
            else -> loadFamousCinemas()
        }

        updateUserLocation()

        // ניקוי הקרנות ישנות
        viewModelScope.launch {
            try {
                cinemaRepository.cleanOldShowtimes()
            } catch (e: Exception) {
                Log.e("CinemaViewModel", "Error cleaning old showtimes", e)
            }
        }
    }

    /**
     * טעינת נתונים ראשוניים
     */
    private fun preloadSampleData() {
        viewModelScope.launch {
            try {
                cinemaRepository.preloadSampleCinemas()
                cinemaRepository.cleanOldShowtimes()
                Log.d("CinemaViewModel", "Sample data preloaded successfully")
            } catch (e: Exception) {
                Log.e("CinemaViewModel", "Error preloading sample data", e)
            }
        }
    }
}