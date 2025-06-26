package com.example.mycinema.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.mycinema.databinding.FragmentMovieDetailsBinding
import com.example.mycinema.viewmodel.MovieViewModel
import com.example.mycinema.util.ImageHelper
import java.text.SimpleDateFormat
import java.util.*

class MovieDetailsFragment : Fragment() {
    private var _b: FragmentMovieDetailsBinding? = null
    private val b get() = _b!!
    private val args: MovieDetailsFragmentArgs by navArgs()
    private val vm: MovieViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMovieDetailsBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        vm.get(args.movieId).observe(viewLifecycleOwner) { m ->
            b.tvTitle.text = m.title
            b.tvDescription.text = m.description
            b.tvGenre.text = m.genre ?: "Unknown"
            b.tvActors.text = m.actors ?: "Unknown"
            b.tvDirector.text = m.director ?: "Unknown"
            b.tvShowtime.text = m.showtime

            m.releaseDate?.let {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    val date = inputFormat.parse(it)
                    date?.let {
                        b.tvReleaseDate.text = outputFormat.format(date)
                    } ?: run {
                        b.tvReleaseDate.text = it
                    }
                } catch (e: Exception) {
                    b.tvReleaseDate.text = it
                }
            } ?: run {
                b.tvReleaseDate.text = "Unknown"
            }

            m.duration?.let {
                b.tvDuration.text = "$it minutes"
            } ?: run {
                b.tvDuration.text = "Unknown"
            }

            m.rating?.let {
                val ratingOutOf5 = it / 2f
                b.ratingBar.rating = ratingOutOf5
                b.tvRatingValue.text = it.toString()
            } ?: run {
                b.ratingBar.rating = 0f
                b.tvRatingValue.text = "N/A"
            }

            context?.let { ctx ->
                ImageHelper.loadMovieImageByTitle(ctx, m.title, b.ivPoster)
            }

            b.btnFavorites.setImageResource(
                if (m.isFavorite) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )

            b.btnFavorites.setOnClickListener {
                vm.toggleFavorite(m)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}