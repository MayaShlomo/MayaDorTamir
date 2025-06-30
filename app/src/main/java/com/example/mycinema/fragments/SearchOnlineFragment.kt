package com.example.mycinema.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycinema.R
import com.example.mycinema.adapter.OnlineMovieAdapter
import com.example.mycinema.databinding.FragmentSearchOnlineBinding
import com.example.mycinema.models.ApiMovie
import com.example.mycinema.viewmodel.MovieViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchOnlineFragment : Fragment() {

    private var _binding: FragmentSearchOnlineBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieViewModel by viewModels()
    private lateinit var adapter: OnlineMovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchOnlineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("SearchOnlineFragment", "Fragment created")

        setupRecyclerView()
        setupSearchView()
        observeViewModel()
        setupSwipeRefresh()

        // טען סרטים פופולריים בהתחלה
        viewModel.loadPopularMovies()
    }

    private fun setupRecyclerView() {
        adapter = OnlineMovieAdapter(
            onMovieClick = { apiMovie ->
                // *** תיקון עיקרי - ניווט למסך פרטים מלא! ***
                Log.d("SearchOnlineFragment", "Movie clicked: ${apiMovie.title} - navigating to details")
                navigateToMovieDetails(apiMovie)
            },
            onAddToLocalClick = { apiMovie ->
                // הוספה לאוסף מקומי
                Log.d("SearchOnlineFragment", "Adding movie to local collection: ${apiMovie.title}")
                addMovieToCollection(apiMovie)
            }
        )

        binding.recyclerViewOnline.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchOnlineFragment.adapter
        }
    }

    /**
     * ניווט למסך פרטים מלא עם נתוני API
     */
    private fun navigateToMovieDetails(apiMovie: ApiMovie) {
        try {
            Log.d("SearchOnlineFragment", "Creating details bundle for: ${apiMovie.title}")

            // יצירת סרט זמני ושמירתו למסד נתונים
            viewModel.addApiMovieToLocal(apiMovie)

            // המתנה קצרה ואז ניווט (הסרט ישמר עם ID חדש)
            // נשתמש בפתרון זמני - ניווט למסך הוספה עם נתוני API
            val bundle = Bundle().apply {
                putInt("movieId", 0) // סרט חדש
                putString("apiTitle", apiMovie.title)
                putString("apiDescription", apiMovie.overview)
                putString("apiImageUrl", com.example.mycinema.network.MovieApiService.getPosterUrl(apiMovie.posterPath))
                putString("apiReleaseDate", apiMovie.releaseDate)
                putFloat("apiRating", apiMovie.voteAverage)
                putInt("apiId", apiMovie.id)
                putString("apiGenres", com.example.mycinema.models.GenreMapper.getGenreNames(apiMovie.genreIds))
                putBoolean("viewOnly", true) // מצב צפייה בלבד
            }

            findNavController().navigate(
                R.id.action_searchOnline_to_addEditMovie,
                bundle
            )

        } catch (e: Exception) {
            Log.e("SearchOnlineFragment", "Error navigating to movie details", e)
            // גיבוי - הצגת פרטים בדיאלוג
            showMovieDetailsDialog(apiMovie)
        }
    }

    /**
     * הוספה מהירה לאוסף
     */
    private fun addMovieToCollection(apiMovie: ApiMovie) {
        viewModel.addApiMovieToLocal(apiMovie)

        Toast.makeText(
            requireContext(),
            "הסרט '${apiMovie.title}' נוסף לאוסף! 🎉",
            Toast.LENGTH_LONG
        ).show()
    }

    /**
     * דיאלוג גיבוי במקרה של כשל בניווט
     */
    private fun showMovieDetailsDialog(apiMovie: ApiMovie) {
        val message = buildString {
            append("🎭 כותרת: ${apiMovie.title}\n\n")

            if (apiMovie.overview.isNotBlank()) {
                append("📖 תיאור:\n${apiMovie.overview}\n\n")
            }

            // שנה
            val year = if (apiMovie.releaseDate.isNullOrBlank()) "לא ידוע"
            else apiMovie.releaseDate.substring(0, 4)
            append("📅 שנת יציאה: $year\n")

            // דירוג
            append("⭐ דירוג: ${String.format("%.1f", apiMovie.voteAverage)}/10\n")

            // ז'אנרים
            val genres = com.example.mycinema.models.GenreMapper.getGenreNames(apiMovie.genreIds)
            if (genres.isNotBlank()) {
                append("🎬 ז'אנרים: $genres\n")
            }

            append("\n💡 רוצה לשמור את הסרט? הוסף אותו לאוסף שלך!")
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("פרטי הסרט")
            .setMessage(message)
            .setPositiveButton("הוסף לאוסף") { _, _ ->
                addMovieToCollection(apiMovie)
            }
            .setNeutralButton("הוסף וערוך") { _, _ ->
                navigateToAddEditWithApiMovie(apiMovie)
            }
            .setNegativeButton("סגור", null)
            .show()
    }

    /**
     * ניווט למסך עריכה עם נתוני API
     */
    private fun navigateToAddEditWithApiMovie(apiMovie: ApiMovie) {
        val bundle = Bundle().apply {
            putInt("movieId", 0) // סרט חדש
            putString("apiTitle", apiMovie.title)
            putString("apiDescription", apiMovie.overview)
            putString("apiImageUrl", com.example.mycinema.network.MovieApiService.getPosterUrl(apiMovie.posterPath))
            putString("apiReleaseDate", apiMovie.releaseDate)
            putFloat("apiRating", apiMovie.voteAverage)
            putInt("apiId", apiMovie.id)
            putString("apiGenres", com.example.mycinema.models.GenreMapper.getGenreNames(apiMovie.genreIds))
        }

        findNavController().navigate(
            R.id.action_searchOnline_to_addEditMovie,
            bundle
        )
    }

    private fun setupSearchView() {
        binding.searchViewOnline.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotBlank()) {
                        viewModel.searchMoviesOnline(it.trim())
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    viewModel.clearOnlineSearch()
                } else if (newText.length >= 3) {
                    viewModel.searchMoviesOnline(newText.trim())
                }
                return true
            }
        })

        binding.btnClearSearch.setOnClickListener {
            binding.searchViewOnline.setQuery("", false)
            viewModel.clearOnlineSearch()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (binding.searchViewOnline.query.isNullOrBlank()) {
                viewModel.refreshPopularMovies()
            } else {
                viewModel.searchMoviesOnline(binding.searchViewOnline.query.toString())
            }
        }
    }

    private fun observeViewModel() {
        viewModel.onlineMovies.observe(viewLifecycleOwner) { movies ->
            Log.d("SearchOnlineFragment", "Received ${movies.size} movies")
            adapter.submitList(movies)
            updateEmptyState(movies.isEmpty() && !viewModel.loading.value!!)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("SearchOnlineFragment", "Loading state: $isLoading")
            binding.progressBar.isVisible = isLoading
            binding.swipeRefreshLayout.isRefreshing = isLoading && binding.swipeRefreshLayout.isRefreshing

            if (!isLoading) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                Log.e("SearchOnlineFragment", "Error: $errorMessage")
                showError(errorMessage)
                updateEmptyState(true)
            }
        }

        viewModel.isOnlineSearchActive.observe(viewLifecycleOwner) { isActive ->
            binding.tvSearchStatus.text = if (isActive) {
                getString(R.string.search_results)
            } else {
                getString(R.string.popular_movies)
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.recyclerViewOnline.isVisible = !isEmpty
        binding.layoutEmptyState.isVisible = isEmpty

        if (isEmpty) {
            val isSearchActive = viewModel.isOnlineSearchActive.value ?: false
            if (isSearchActive) {
                binding.tvEmptyMessage.text = getString(R.string.no_search_results)
                binding.tvEmptySubMessage.text = getString(R.string.try_different_search)
            } else {
                binding.tvEmptyMessage.text = getString(R.string.no_popular_movies)
                binding.tvEmptySubMessage.text = getString(R.string.check_internet_connection)
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "⚠️ שגיאה: $message", Toast.LENGTH_LONG).show()
        viewModel.clearError()
    }

    override fun onResume() {
        super.onResume()
        Log.d("SearchOnlineFragment", "Fragment resumed")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}