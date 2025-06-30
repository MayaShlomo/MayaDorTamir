package com.example.mycinema.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mycinema.R
import com.example.mycinema.databinding.FragmentStatisticsBinding
import com.example.mycinema.models.Movie
import com.example.mycinema.viewmodel.MovieViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeData()
    }

    private fun setupUI() {
        // כפתור רענון
        binding.btnRefresh.setOnClickListener {
            refreshStatistics()
        }
    }

    private fun observeData() {
        viewModel.allMovies.observe(viewLifecycleOwner) { movies ->
            Log.d("StatisticsFragment", "Movies loaded: ${movies.size}")
            updateStatistics(movies)
        }

        viewModel.favoriteMovies.observe(viewLifecycleOwner) { favorites ->
            Log.d("StatisticsFragment", "Favorites loaded: ${favorites.size}")
            updateFavoriteStatistics(favorites)
        }
    }

    private fun updateStatistics(movies: List<Movie>) {
        // סטטיסטיקות בסיסיות
        binding.tvTotalMovies.text = "${movies.size}"

        // דירוג ממוצע
        val avgRating = movies.mapNotNull { it.rating }.average()
        if (avgRating.isNaN()) {
            binding.tvAverageRating.text = getString(R.string.not_available)
        } else {
            binding.tvAverageRating.text = String.format("%.1f", avgRating)
        }

        // משך כולל
        val totalDuration = movies.mapNotNull { it.duration }.sum()
        val hours = totalDuration / 60
        val minutes = totalDuration % 60

        binding.tvTotalDuration.text = when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "$totalDuration minutes"
        }

        // פירוט לפי ז'אנרים
        updateGenreBreakdown(movies)
    }

    private fun updateFavoriteStatistics(favorites: List<Movie>) {
        binding.tvFavoriteCount.text = "${favorites.size}"

        // אחוז מועדפים
        val allMoviesCount = viewModel.allMovies.value?.size ?: 0
        if (allMoviesCount > 0) {
            val favoritePercentage = (favorites.size * 100.0) / allMoviesCount
            binding.tvFavoritePercentage.text = "${String.format("%.1f", favoritePercentage)}%"
        } else {
            binding.tvFavoritePercentage.text = "0%"
        }
    }

    private fun updateGenreBreakdown(movies: List<Movie>) {
        val genreCount = movies
            .mapNotNull { it.genre }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }

        if (genreCount.isNotEmpty()) {
            // הז'אנר הפופולרי ביותר
            val mostCommonGenre = genreCount.first()
            binding.tvMostCommonGenre.text = "${mostCommonGenre.first} (${mostCommonGenre.second})"

            // הצגת כל הז'אנרים
            val genreText = when {
                genreCount.size == 1 -> "${genreCount[0].first}: ${genreCount[0].second} movies"
                genreCount.size <= 3 -> genreCount.joinToString("\n") { "${it.first}: ${it.second} movies" }
                else -> {
                    val top3 = genreCount.take(3)
                    val others = genreCount.drop(3).sumOf { it.second }
                    top3.joinToString("\n") { "${it.first}: ${it.second} movies" } +
                            "\nOthers: $others movies"
                }
            }
            binding.tvGenreBreakdown.text = genreText
        } else {
            binding.tvMostCommonGenre.text = getString(R.string.not_available)
            binding.tvGenreBreakdown.text = getString(R.string.no_data_available)
        }
    }

    private fun refreshStatistics() {
        // רענון הנתונים מה-ViewModel
        viewModel.refreshFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}