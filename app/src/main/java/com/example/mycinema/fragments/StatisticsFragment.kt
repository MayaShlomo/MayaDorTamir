package com.example.mycinema.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
        binding.tvTotalMovies.text = "Total Movies: ${movies.size}"

        // דירוג ממוצע
        val avgRating = movies.mapNotNull { it.rating }.average()
        if (avgRating.isNaN()) {
            binding.tvAverageRating.text = "Average Rating: N/A"
        } else {
            binding.tvAverageRating.text = "Average Rating: ${String.format("%.1f", avgRating)}"
        }

        // משך כולל
        val totalDuration = movies.mapNotNull { it.duration }.sum()
        binding.tvTotalDuration.text = "Total Duration: $totalDuration minutes"

        // פירוט לפי ז'אנרים
        updateGenreBreakdown(movies)
    }

    private fun updateFavoriteStatistics(favorites: List<Movie>) {
        binding.tvFavoriteCount.text = "Favorite Movies: ${favorites.size}"

        // אחוז מועדפים
        val allMoviesCount = viewModel.allMovies.value?.size ?: 0
        if (allMoviesCount > 0) {
            val favoritePercentage = (favorites.size * 100.0) / allMoviesCount
            binding.tvFavoritePercentage.text = "Percentage: ${String.format("%.1f%%", favoritePercentage)}"
        } else {
            binding.tvFavoritePercentage.text = "Percentage: 0%"
        }
    }

    private fun updateGenreBreakdown(movies: List<Movie>) {
        val genreCount = movies
            .mapNotNull { it.genre }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }

        if (genreCount.isNotEmpty()) {
            // הז'אנר הפופולרי ביותר
            val mostCommonGenre = genreCount.first()
            binding.tvMostCommonGenre.text = "Most Common: ${mostCommonGenre.first} (${mostCommonGenre.second})"

            // הצגת 3 הז'אנרים המובילים
            val topGenres = genreCount.take(3)
            val genreText = topGenres.joinToString("\n") { "${it.first}: ${it.second} movies" }
            binding.tvGenreBreakdown.text = genreText
        } else {
            binding.tvMostCommonGenre.text = "Most Common: N/A"
            binding.tvGenreBreakdown.text = "No data available"
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