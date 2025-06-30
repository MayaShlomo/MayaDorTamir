package com.example.mycinema.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "cinemas")
data class Cinema(
    @PrimaryKey val id: Int,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val phone: String?,
    val website: String?,
    val imageUrl: String?,
    val rating: Float = 0f,
    val isOpen: Boolean = true
)

@Entity(
    tableName = "showtimes",
    foreignKeys = [
        ForeignKey(
            entity = Cinema::class,
            parentColumns = ["id"],
            childColumns = ["cinemaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cinemaId"]),
        Index(value = ["showDate"]),
        Index(value = ["movieTitle"])
    ]
)
data class Showtime(
    @PrimaryKey val id: Int,
    val cinemaId: Int,
    val movieTitle: String,
    val showDate: String, // YYYY-MM-DD format
    val showTime: String, // HH:MM format
    val price: Float?,
    val isAvailable: Boolean = true,
    val movieId: Int? = null // קישור לסרט במסד המקומי
)

// Data classes for UI
data class CinemaWithDistance(
    val cinema: Cinema,
    val distance: Double // בקילומטרים
)

data class CinemaWithShowtimes(
    val cinema: Cinema,
    val showtimes: List<Showtime>
)

// Location data class
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)