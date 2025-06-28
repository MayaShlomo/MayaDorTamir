// SearchOnlineFragment.kt - שיפור הזרימה
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
                // הצגת דיאלוג עם אפשרויות
                showMovieOptionsDialog(apiMovie)
            },
            onAddToLocalClick = { apiMovie ->
                // במקום להוסיף ישירות, נעבור למסך עריכה
                navigateToAddEditWithApiMovie(apiMovie)
            }
        )

        binding.recyclerViewOnline.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SearchOnlineFragment.adapter
        }
    }

    private fun showMovieOptionsDialog(apiMovie: ApiMovie) {
        val options = arrayOf(
            getString(R.string.view_details),
            getString(R.string.add_to_collection),
            getString(R.string.add_and_edit)
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(apiMovie.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showMovieDetails(apiMovie)
                    1 -> quickAddToCollection(apiMovie)
                    2 -> navigateToAddEditWithApiMovie(apiMovie)
                }
            }
            .show()
    }

    private fun showMovieDetails(apiMovie: ApiMovie) {
        // הצגת פרטים בדיאלוג
        val message = buildString {
            append(apiMovie.overview ?: getString(R.string.no_description_available))
            append("\n\n")
            append(getString(R.string.year_format, apiMovie.releaseDate?.substring(0, 4) ?: getString(R.string.unknown)))
            append("\n")
            append(getString(R.string.rating_format, String.format("%.1f", apiMovie.voteAverage)))
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(apiMovie.title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.add_to_collection)) { _, _ ->
                navigateToAddEditWithApiMovie(apiMovie)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun quickAddToCollection(apiMovie: ApiMovie) {
        // הוספה מהירה עם האפשרות לערוך אחר כך
        viewModel.addApiMovieToLocal(apiMovie)

        Toast.makeText(
            requireContext(),
            getString(R.string.movie_added_tap_to_edit),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun navigateToAddEditWithApiMovie(apiMovie: ApiMovie) {
        // העברת המידע מה-API למסך העריכה
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
                // חיפוש בזמן אמת רק אם יש יותר מ-2 תווים
                if (newText.isNullOrBlank()) {
                    viewModel.clearOnlineSearch()
                } else if (newText.length >= 3) {
                    viewModel.searchMoviesOnline(newText.trim())
                }
                return true
            }
        })

        // כפתור ניקוי חיפוש
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
        // תוצאות חיפוש/סרטים פופולריים
        viewModel.onlineMovies.observe(viewLifecycleOwner) { movies ->
            Log.d("SearchOnlineFragment", "Received ${movies.size} movies")
            adapter.submitList(movies)
            updateEmptyState(movies.isEmpty() && !viewModel.loading.value!!)
        }

        // מצב טעינה
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            Log.d("SearchOnlineFragment", "Loading state: $isLoading")
            binding.progressBar.isVisible = isLoading
            binding.swipeRefreshLayout.isRefreshing = isLoading && binding.swipeRefreshLayout.isRefreshing

            if (!isLoading) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        // שגיאות
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                Log.e("SearchOnlineFragment", "Error: $errorMessage")
                showError(errorMessage)
                updateEmptyState(true)
            }
        }

        // מצב חיפוש פעיל
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

        // עדכון הודעת empty state
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
        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
        // ניקוי השגיאה אחרי הצגה
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