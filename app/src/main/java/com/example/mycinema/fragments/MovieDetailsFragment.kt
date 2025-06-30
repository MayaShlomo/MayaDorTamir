// החלף את MovieDetailsFragment שלך בזה:

package com.example.mycinema.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.example.mycinema.R
import com.example.mycinema.databinding.FragmentMovieDetailsBinding
import com.example.mycinema.viewmodel.MovieViewModel
import com.example.mycinema.util.ImageHelper
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MovieDetailsFragment : Fragment() {
    private var _b: FragmentMovieDetailsBinding? = null
    private val b get() = _b!!
    private val vm: MovieViewModel by activityViewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMovieDetailsBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        Log.d("MovieDetailsFragment", "=== FRAGMENT CREATED ===")

        // קבלת movieId בכמה דרכים שונות
        val movieId = getMovieId()

        if (movieId <= 0) {
            Log.e("MovieDetailsFragment", "❌ Invalid movie ID: $movieId")
            return
        }

        Log.d("MovieDetailsFragment", "✅ Loading movie with ID: $movieId")

        // צפייה בסרט הספציפי
        vm.getMovieById(movieId).observe(viewLifecycleOwner) { movie ->
            if (movie != null) {
                Log.d("MovieDetailsFragment", "🎬 Movie loaded: ${movie.title}")
                displayMovieDetails(movie)
            } else {
                Log.e("MovieDetailsFragment", "❌ Movie not found for ID: $movieId")
                // אפשר לחזור למסך הקודם
            }
        }
    }

    private fun getMovieId(): Int {
        // נסיון 1: Safe Args
        try {
            val args: MovieDetailsFragmentArgs by navArgs()
            val movieId = args.movieId
            Log.d("MovieDetailsFragment", "✅ Got movieId from Safe Args: $movieId")
            return movieId
        } catch (e: Exception) {
            Log.w("MovieDetailsFragment", "⚠️ Safe Args failed: ${e.message}")
        }

        // נסיון 2: Arguments Bundle
        try {
            val movieId = arguments?.getInt("movieId", 0) ?: 0
            if (movieId > 0) {
                Log.d("MovieDetailsFragment", "✅ Got movieId from Bundle: $movieId")
                return movieId
            }
        } catch (e: Exception) {
            Log.w("MovieDetailsFragment", "⚠️ Bundle failed: ${e.message}")
        }

        // נסיון 3: RequireArguments
        try {
            val movieId = requireArguments().getInt("movieId", 0)
            if (movieId > 0) {
                Log.d("MovieDetailsFragment", "✅ Got movieId from requireArguments: $movieId")
                return movieId
            }
        } catch (e: Exception) {
            Log.w("MovieDetailsFragment", "⚠️ RequireArguments failed: ${e.message}")
        }

        Log.e("MovieDetailsFragment", "❌ Could not get movieId from any source!")
        return 0
    }

    private fun displayMovieDetails(movie: com.example.mycinema.models.Movie) {
        Log.d("MovieDetailsFragment", "📝 Displaying details for: ${movie.title}")

        b.apply {
            tvTitle.text = movie.title
            tvDescription.text = movie.description.takeIf { !it.isNullOrBlank() }
                ?: getString(R.string.no_description_available)
            tvGenre.text = movie.genre.takeIf { !it.isNullOrBlank() }
                ?: getString(R.string.unknown)
            tvActors.text = movie.actors.takeIf { !it.isNullOrBlank() }
                ?: getString(R.string.unknown)
            tvDirector.text = movie.director.takeIf { !it.isNullOrBlank() }
                ?: getString(R.string.unknown)
            tvShowtime.text = movie.showtime.takeIf { !it.isNullOrBlank() }
                ?: getString(R.string.not_available)

            // תאריך יציאה
            movie.releaseDate?.let { dateString ->
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    val date = inputFormat.parse(dateString)
                    tvReleaseDate.text = date?.let { outputFormat.format(it) } ?: dateString
                } catch (e: Exception) {
                    tvReleaseDate.text = dateString
                }
            } ?: run {
                tvReleaseDate.text = getString(R.string.unknown)
            }

            // משך הסרט
            movie.duration?.let { duration ->
                tvDuration.text = "$duration דקות"
            } ?: run {
                tvDuration.text = getString(R.string.unknown)
            }

            // דירוג
            movie.rating?.let { rating ->
                val ratingOutOf5 = rating / 2f
                ratingBar.rating = ratingOutOf5
                tvRatingValue.text = rating.toString()
            } ?: run {
                ratingBar.rating = 0f
                tvRatingValue.text = getString(R.string.no_rating)
            }

            // תמונה
            context?.let { ctx ->
                ImageHelper.loadMovieImageByTitle(ctx, movie.title, ivPoster)
            }

            // כפתור מועדפים
            updateFavoriteButton(movie.isFavorite)

            btnFavorites.setOnClickListener {
                Log.d("MovieDetailsFragment", "⭐ Favorite toggled for: ${movie.title}")
                vm.toggleFavorite(movie)
            }
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        b.btnFavorites.setImageResource(
            if (isFavorite) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}