package com.example.mycinema.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val apiId: Int? = null, // ID מ-TMDb API
    val title: String,
    val description: String,
    val genre: String?,
    val actors: String?,
    val director: String?,
    val year: Int?,
    val rating: Float?,
    val imageUri: String?, // יכול להיות URL או URI מקומי
    val showtime: String,
    val isFavorite: Boolean = false,
    val releaseDate: String?,
    val duration: Int?,
    val isFromApi: Boolean = false // האם הסרט הגיע מ-API
)

// הרחבה לבדיקת סוג התמונה
fun Movie.hasRemoteImage(): Boolean {
    return imageUri?.startsWith("http") == true
}

fun Movie.hasLocalImage(): Boolean {
    return imageUri != null && !hasRemoteImage()
}