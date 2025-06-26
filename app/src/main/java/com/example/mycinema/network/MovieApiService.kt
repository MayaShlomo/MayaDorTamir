package com.example.mycinema.network

import com.example.mycinema.models.MovieDetails
import com.example.mycinema.models.MovieSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
        const val POSTER_SIZE_W500 = "w500"
        const val BACKDROP_SIZE_W780 = "w780"

        fun getPosterUrl(posterPath: String?): String? {
            return if (posterPath != null) {
                "$IMAGE_BASE_URL$POSTER_SIZE_W500$posterPath"
            } else null
        }

        fun getBackdropUrl(backdropPath: String?): String? {
            return if (backdropPath != null) {
                "$IMAGE_BASE_URL$BACKDROP_SIZE_W780$backdropPath"
            } else null
        }
    }

    // בקשה 1: חיפוש סרטים
    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<MovieSearchResponse>

    // בקשה 2: סרטים פופולריים
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<MovieSearchResponse>

    // בקשה 3: פרטי סרט ספציפי
    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Response<MovieDetails>

    // בונוס: סרטים מומלצים
    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<MovieSearchResponse>

    // בונוס: סרטים עכשיו בקולנוע
    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Response<MovieSearchResponse>
}