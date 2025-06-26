package com.example.mycinema.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val genre: String?,
    val actors: String?,
    val director: String?,
    val year: Int?,
    val rating: Float?,
    val imageUri: String?,
    val showtime: String,
    val isFavorite: Boolean = false,
    val releaseDate: String?,
    val duration: Int?
)