package com.example.mycinema.models

import com.google.gson.annotations.SerializedName

// תגובת חיפוש סרטים מה-API
data class MovieSearchResponse(
    val page: Int,
    val results: List<ApiMovie>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)

// סרט מה-API
data class ApiMovie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Float,
    @SerializedName("vote_count")
    val voteCount: Int,
    @SerializedName("genre_ids")
    val genreIds: List<Int>,
    val popularity: Float,
    val adult: Boolean,
    @SerializedName("original_language")
    val originalLanguage: String,
    @SerializedName("original_title")
    val originalTitle: String,
    val video: Boolean
)

// פרטי סרט מורחבים
data class MovieDetails(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Float,
    @SerializedName("vote_count")
    val voteCount: Int,
    val genres: List<Genre>,
    val runtime: Int?,
    val budget: Long,
    val revenue: Long,
    val status: String,
    val tagline: String?,
    @SerializedName("production_companies")
    val productionCompanies: List<ProductionCompany>
)

// ז'אנר
data class Genre(
    val id: Int,
    val name: String
)

// חברת הפקה
data class ProductionCompany(
    val id: Int,
    val name: String,
    @SerializedName("logo_path")
    val logoPath: String?,
    @SerializedName("origin_country")
    val originCountry: String
)

// מפת ז'אנרים (ID לשם)
object GenreMapper {
    private val genreMap = mapOf(
        28 to "Action",
        12 to "Adventure",
        16 to "Animation",
        35 to "Comedy",
        80 to "Crime",
        99 to "Documentary",
        18 to "Drama",
        10751 to "Family",
        14 to "Fantasy",
        36 to "History",
        27 to "Horror",
        10402 to "Music",
        9648 to "Mystery",
        10749 to "Romance",
        878 to "Science Fiction",
        10770 to "TV Movie",
        53 to "Thriller",
        10752 to "War",
        37 to "Western"
    )

    fun getGenreName(id: Int): String {
        return genreMap[id] ?: "Unknown"
    }

    fun getGenreNames(ids: List<Int>): String {
        return ids.map { getGenreName(it) }.joinToString(", ")
    }
}