package com.example.mycinema.util

import android.content.Context
import android.widget.ImageView
import android.util.Log
import com.example.mycinema.R

object ImageHelper {
    private val movieImageMap = mapOf(
        "Inception" to R.drawable.inception,
        "The Shawshank Redemption" to R.drawable.shawshank,
        "The Dark Knight" to R.drawable.dark_knight,
        "Pulp Fiction" to R.drawable.pulp_fiction,
        "Parasite" to R.drawable.parasite,
        "Interstellar" to R.drawable.interstellar,
        "The Lion King" to R.drawable.lion_king,
        "The Godfather" to R.drawable.godfather,
        "Jurassic Park" to R.drawable.jurassic_park,
        "Spirited Away" to R.drawable.spirited_away,
        "Get Out" to R.drawable.get_out,
        "La La Land" to R.drawable.la_la_land,
        "The Matrix" to R.drawable.matrix,
        "Titanic" to R.drawable.titanic,
        "Whiplash" to R.drawable.whiplash,
        "Avengers: Endgame" to R.drawable.avengers_endgame,
        "The Grand Budapest Hotel" to R.drawable.grand_budapest,
        "Soul" to R.drawable.soul,
        "The Silence of the Lambs" to R.drawable.silence_of_the_lambs,
        "Roma" to R.drawable.roma
    )

    fun loadMovieImageByTitle(context: Context, title: String, imageView: ImageView) {
        try {
            Log.d("ImageHelper", "Loading image for movie: $title")

            if (title.contains("Inception", ignoreCase = true)) {
                imageView.setImageResource(R.drawable.inception)
                Log.d("ImageHelper", "Set inception image for movie: $title")
                return
            }

            val resourceId = movieImageMap[title] ?: R.drawable.default_movie
            imageView.setImageResource(resourceId)
            Log.d("ImageHelper", "Set image resource for movie: $title")
        } catch (e: Exception) {
            Log.e("ImageHelper", "Error loading image for $title: ${e.message}", e)
            imageView.setImageResource(R.drawable.default_movie)
        }
    }
}