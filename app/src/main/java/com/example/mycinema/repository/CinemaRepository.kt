// CinemaRepository.kt
// החלף את: app/src/main/java/com/example/mycinema/repository/CinemaRepository.kt
package com.example.mycinema.repository

import android.content.Context
import android.util.Log
import com.example.mycinema.data.CinemaDao
import com.example.mycinema.data.FamousCinemasData
import com.example.mycinema.models.*
import com.example.mycinema.services.LocationService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CinemaRepository @Inject constructor(
    private val cinemaDao: CinemaDao,
    private val locationService: LocationService,
    @ApplicationContext private val context: Context
) {

    // ================================
    // פונקציות חדשות - בתי קולנוע מפורסמים
    // ================================

    /**
     * קבלת בתי קולנוע מפורסמים בקרבת מקום
     */
    suspend fun getFamousCinemasNearby(radiusKm: Double = 100.0): List<CinemaWithDistance> =
        withContext(Dispatchers.IO) {
            val currentLocation = locationService.getCurrentLocation()

            return@withContext if (currentLocation != null) {
                Log.d("CinemaRepository", "Finding famous cinemas near: ${currentLocation.latitude}, ${currentLocation.longitude}")

                val famousCinemas = FamousCinemasData.getCinemasNear(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    radiusKm
                )

                famousCinemas.map { cinema ->
                    val distance = locationService.calculateDistance(
                        currentLocation.latitude, currentLocation.longitude,
                        cinema.latitude, cinema.longitude
                    )
                    CinemaWithDistance(cinema, distance)
                }.sortedBy { it.distance }

            } else {
                Log.w("CinemaRepository", "No location available, returning cinemas by region")
                // אם אין מיקום, החזר בתי קולנוע ישראליים כברירת מחדל
                val israeliCinemas = FamousCinemasData.getCinemasByCountry("israel")
                israeliCinemas.map { CinemaWithDistance(it, 0.0) }
            }
        }

    /**
     * קבלת כל בתי הקולנוע המפורסמים
     */
    suspend fun getAllFamousCinemas(): List<CinemaWithDistance> =
        withContext(Dispatchers.IO) {
            val currentLocation = locationService.getCurrentLocation()
            val allCinemas = FamousCinemasData.getFamousCinemas()

            return@withContext if (currentLocation != null) {
                allCinemas.map { cinema ->
                    val distance = locationService.calculateDistance(
                        currentLocation.latitude, currentLocation.longitude,
                        cinema.latitude, cinema.longitude
                    )
                    CinemaWithDistance(cinema, distance)
                }.sortedBy { it.distance }
            } else {
                allCinemas.map { CinemaWithDistance(it, 0.0) }
            }
        }

    /**
     * קבלת בתי קולנוע לפי מדינה
     */
    suspend fun getCinemasByCountry(country: String): List<CinemaWithDistance> =
        withContext(Dispatchers.IO) {
            val currentLocation = locationService.getCurrentLocation()
            val cinemas = FamousCinemasData.getCinemasByCountry(country)

            return@withContext if (currentLocation != null) {
                cinemas.map { cinema ->
                    val distance = locationService.calculateDistance(
                        currentLocation.latitude, currentLocation.longitude,
                        cinema.latitude, cinema.longitude
                    )
                    CinemaWithDistance(cinema, distance)
                }.sortedBy { it.distance }
            } else {
                cinemas.map { CinemaWithDistance(it, 0.0) }
            }
        }

    /**
     * חיפוש בתי קולנוע מפורסמים לפי שם
     */
    suspend fun searchFamousCinemas(query: String): List<CinemaWithDistance> =
        withContext(Dispatchers.IO) {
            val currentLocation = locationService.getCurrentLocation()
            val allCinemas = FamousCinemasData.getFamousCinemas()

            val filteredCinemas = allCinemas.filter { cinema ->
                cinema.name.contains(query, ignoreCase = true) ||
                        cinema.address.contains(query, ignoreCase = true)
            }

            return@withContext if (currentLocation != null) {
                filteredCinemas.map { cinema ->
                    val distance = locationService.calculateDistance(
                        currentLocation.latitude, currentLocation.longitude,
                        cinema.latitude, cinema.longitude
                    )
                    CinemaWithDistance(cinema, distance)
                }.sortedBy { it.distance }
            } else {
                filteredCinemas.map { CinemaWithDistance(it, 0.0) }
            }
        }

    /**
     * קבלת בית קולנוע מפורסם ספציפי (ללא הקרנות)
     */
    suspend fun getFamousCinemaById(cinemaId: Int): Cinema? =
        withContext(Dispatchers.IO) {
            FamousCinemasData.getFamousCinemas().find { it.id == cinemaId }
        }

    /**
     * שילוב בתי קולנוע מקומיים + מפורסמים
     */
    suspend fun getCombinedCinemas(radiusKm: Double = 50.0): List<CinemaWithDistance> =
        withContext(Dispatchers.IO) {
            val localCinemas = getCinemasNearby(radiusKm) // הנתונים המקומיים הקיימים
            val famousCinemas = getFamousCinemasNearby(radiusKm)

            // שילוב ומניעת כפילויות לפי שם ומיקום
            val combinedMap = mutableMapOf<String, CinemaWithDistance>()

            // הוסף קולנועים מקומיים
            localCinemas.forEach { cinemaWithDistance ->
                val key = "${cinemaWithDistance.cinema.name.lowercase()}-${cinemaWithDistance.cinema.latitude}-${cinemaWithDistance.cinema.longitude}"
                combinedMap[key] = cinemaWithDistance
            }

            // הוסף קולנועים מפורסמים (רק אם לא קיימים כבר)
            famousCinemas.forEach { cinemaWithDistance ->
                val key = "${cinemaWithDistance.cinema.name.lowercase()}-${cinemaWithDistance.cinema.latitude}-${cinemaWithDistance.cinema.longitude}"
                if (!combinedMap.containsKey(key)) {
                    combinedMap[key] = cinemaWithDistance
                }
            }

            return@withContext combinedMap.values.sortedBy { it.distance }
        }

    // ================================
    // פונקציות קיימות - נתונים מקומיים
    // ================================

    /**
     * קבלת בתי קולנוע בקרבת מקום עם מרחקים (נתונים מקומיים)
     */
    suspend fun getCinemasNearby(radiusKm: Double = 15.0): List<CinemaWithDistance> =
        withContext(Dispatchers.IO) {
            val currentLocation = locationService.getCurrentLocation()
            val allCinemas = cinemaDao.getAllActiveCinemas()

            return@withContext if (currentLocation != null) {
                Log.d("CinemaRepository", "Current location: ${currentLocation.latitude}, ${currentLocation.longitude}")

                allCinemas.mapNotNull { cinema ->
                    val distance = locationService.calculateDistance(
                        currentLocation.latitude, currentLocation.longitude,
                        cinema.latitude, cinema.longitude
                    )

                    if (distance <= radiusKm) {
                        CinemaWithDistance(cinema, distance)
                    } else {
                        null
                    }
                }.sortedBy { it.distance }
            } else {
                Log.w("CinemaRepository", "No location available, returning all cinemas")
                allCinemas.map { CinemaWithDistance(it, 0.0) }
            }
        }

    /**
     * קבלת בית קולנוע עם הקרנות עתידיות
     */
    suspend fun getCinemaWithShowtimes(cinemaId: Int): CinemaWithShowtimes? =
        withContext(Dispatchers.IO) {
            // נסה קודם במסד הנתונים המקומי
            val localCinema = cinemaDao.getCinemaById(cinemaId)
            if (localCinema != null) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val showtimes = cinemaDao.getUpcomingShowtimes(cinemaId, today)
                return@withContext CinemaWithShowtimes(localCinema, showtimes)
            }

            // אם לא נמצא במקומי, חפש בבתי הקולנוע המפורסמים
            val famousCinema = getFamousCinemaById(cinemaId)
            if (famousCinema != null) {
                // בתי קולנוע מפורסמים ללא הקרנות
                return@withContext CinemaWithShowtimes(famousCinema, emptyList())
            }

            return@withContext null
        }

    /**
     * חיפוש בתי קולנוע שמקרינים סרט מסוים (רק בנתונים מקומיים)
     */
    suspend fun getCinemasShowingMovie(movieTitle: String): List<CinemaWithDistance> =
        withContext(Dispatchers.IO) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val cinemas = cinemaDao.getCinemasShowingMovie(movieTitle, today)
            val currentLocation = locationService.getCurrentLocation()

            return@withContext if (currentLocation != null) {
                cinemas.map { cinema ->
                    val distance = locationService.calculateDistance(
                        currentLocation.latitude, currentLocation.longitude,
                        cinema.latitude, cinema.longitude
                    )
                    CinemaWithDistance(cinema, distance)
                }.sortedBy { it.distance }
            } else {
                cinemas.map { CinemaWithDistance(it, 0.0) }
            }
        }

    /**
     * טעינת נתונים ראשוניים של בתי קולנוע (לדמו)
     */
    suspend fun preloadSampleCinemas() = withContext(Dispatchers.IO) {
        val existingCount = cinemaDao.getActiveCinemasCount()
        if (existingCount > 0) {
            Log.d("CinemaRepository", "Cinemas already exist, skipping preload")
            return@withContext
        }

        Log.d("CinemaRepository", "Preloading sample cinemas")
        val sampleCinemas = getSampleCinemas()
        cinemaDao.insertCinemas(sampleCinemas)

        // הוספת הקרנות לדוגמה
        val sampleShowtimes = getSampleShowtimes()
        cinemaDao.insertShowtimes(sampleShowtimes)

        Log.d("CinemaRepository", "Preloaded ${sampleCinemas.size} cinemas and ${sampleShowtimes.size} showtimes")
    }

    /**
     * ניקוי הקרנות ישנות
     */
    suspend fun cleanOldShowtimes() = withContext(Dispatchers.IO) {
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
        cinemaDao.cleanOldShowtimes(yesterday)
    }

    // ================================
    // נתונים לדוגמה פרטיים
    // ================================

    private fun getSampleCinemas(): List<Cinema> {
        // בתי קולנוע בתל אביב ובאזור (קואורדינטות אמיתיות)
        return listOf(
            Cinema(
                id = 1,
                name = "סינמה סיטי גלילות",
                address = "קניון גלילות, תל אביב",
                latitude = 32.0668,
                longitude = 34.7924,
                phone = "03-6428888",
                website = "https://www.cinema-city.co.il",
                imageUrl = null,
                rating = 4.2f
            ),
            Cinema(
                id = 2,
                name = "סינמה סיטי אילון",
                address = "קניון אילון, רמת גן",
                latitude = 32.1054,
                longitude = 34.8347,
                phone = "03-7584444",
                website = "https://www.cinema-city.co.il",
                imageUrl = null,
                rating = 4.0f
            ),
            Cinema(
                id = 3,
                name = "לב סינמטק",
                address = "שדרות שאול המלך 2, תל אביב",
                latitude = 32.0719,
                longitude = 34.7856,
                phone = "03-6060800",
                website = "https://www.lev.org.il",
                imageUrl = null,
                rating = 4.5f
            ),
            Cinema(
                id = 4,
                name = "YES פלאנט כפר סבא",
                address = "שד׳ ויצמן 130, כפר סבא",
                latitude = 32.1743,
                longitude = 34.9073,
                phone = "09-7676777",
                website = "https://www.yesplanet.co.il",
                imageUrl = null,
                rating = 4.1f
            ),
            Cinema(
                id = 5,
                name = "HOT סינמה יבנה",
                address = "קניון G יבנה",
                latitude = 31.8706,
                longitude = 34.7375,
                phone = "08-9307777",
                website = null,
                imageUrl = null,
                rating = 3.8f
            )
        )
    }

    private fun getSampleShowtimes(): List<Showtime> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val tomorrow = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))

        return listOf(
            // סינמה סיטי גלילות
            Showtime(1, 1, "Inception", today, "19:00", 42f, true, 1),
            Showtime(2, 1, "Inception", today, "21:30", 42f, true, 1),
            Showtime(3, 1, "The Dark Knight", today, "20:15", 45f, true, 3),
            Showtime(4, 1, "The Shawshank Redemption", tomorrow, "18:00", 40f, true, 2),

            // סינמה סיטי אילון
            Showtime(5, 2, "Inception", today, "18:30", 44f, true, 1),
            Showtime(6, 2, "The Dark Knight", today, "21:00", 44f, true, 3),
            Showtime(7, 2, "The Shawshank Redemption", tomorrow, "19:30", 44f, true, 2),

            // לב סינמטק
            Showtime(8, 3, "Inception", today, "20:00", 50f, true, 1),
            Showtime(9, 3, "The Dark Knight", tomorrow, "19:00", 50f, true, 3),

            // YES פלאנט כפר סבא
            Showtime(10, 4, "The Shawshank Redemption", today, "17:45", 38f, true, 2),
            Showtime(11, 4, "Inception", tomorrow, "20:30", 38f, true, 1),

            // HOT סינמה יבנה
            Showtime(12, 5, "The Dark Knight", today, "19:15", 35f, true, 3),
            Showtime(13, 5, "The Shawshank Redemption", tomorrow, "18:30", 35f, true, 2)
        )
    }
}