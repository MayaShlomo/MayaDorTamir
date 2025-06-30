// החלף את כל ה-MoviesListFragment שלך בזה:

package com.example.mycinema.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycinema.R
import com.example.mycinema.adapter.MovieAdapter
import com.example.mycinema.databinding.FragmentMoviesListBinding
import com.example.mycinema.viewmodel.MovieViewModel
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import android.util.Log

@AndroidEntryPoint
class MoviesListFragment : Fragment() {
    private var _b: FragmentMoviesListBinding? = null
    private val b get() = _b!!
    private val vm: MovieViewModel by activityViewModels()
    private lateinit var clearChip: Chip
    private lateinit var adapter: MovieAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentMoviesListBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        Log.d("MoviesListFragment", "=== FRAGMENT CREATED ===")

        setupRecycler()
        setupSearch()
        setupGenreChips()
        observeData()

        b.fabAdd.setOnClickListener {
            try {
                val action = MoviesListFragmentDirections.actionMoviesListToAddEditMovie(0)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e("MoviesListFragment", "FAB navigation failed: ${e.message}")
            }
        }
    }

    private fun setupRecycler() {
        Log.d("MoviesListFragment", "Setting up recycler...")

        b.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = MovieAdapter(
            onMovieClick = { movie ->
                Log.d("MoviesListFragment", "🎬 MOVIE CLICKED: ${movie.title} (ID: ${movie.id})")

                // פתרון 1: ניווט עם Safe Args
                try {
                    Log.d("MoviesListFragment", "Trying Safe Args navigation...")
                    val action = MoviesListFragmentDirections.actionMoviesListToMovieDetails(movie.id)
                    findNavController().navigate(action)
                    Log.d("MoviesListFragment", "✅ Safe Args navigation SUCCESS!")
                    return@MovieAdapter
                } catch (e: Exception) {
                    Log.e("MoviesListFragment", "❌ Safe Args failed: ${e.message}")
                }

                // פתרון 2: ניווט עם Bundle
                try {
                    Log.d("MoviesListFragment", "Trying Bundle navigation...")
                    val bundle = Bundle().apply {
                        putInt("movieId", movie.id)
                    }
                    findNavController().navigate(R.id.movieDetailsFragment, bundle)
                    Log.d("MoviesListFragment", "✅ Bundle navigation SUCCESS!")
                    return@MovieAdapter
                } catch (e: Exception) {
                    Log.e("MoviesListFragment", "❌ Bundle navigation failed: ${e.message}")
                }

                // פתרון 3: ניווט גלובלי
                try {
                    Log.d("MoviesListFragment", "Trying Global navigation...")
                    val bundle = Bundle().apply {
                        putInt("movieId", movie.id)
                    }
                    findNavController().navigate(R.id.action_global_movieDetails, bundle)
                    Log.d("MoviesListFragment", "✅ Global navigation SUCCESS!")
                    return@MovieAdapter
                } catch (e: Exception) {
                    Log.e("MoviesListFragment", "❌ Global navigation failed: ${e.message}")
                }

                // פתרון 4: הודעת שגיאה
                Log.e("MoviesListFragment", "🔥 ALL NAVIGATION METHODS FAILED!")
                Toast.makeText(requireContext(), "לא ניתן לפתוח פרטי סרט כרגע", Toast.LENGTH_SHORT).show()
            },
            onFavoriteClick = { movie ->
                Log.d("MoviesListFragment", "⭐ Favorite clicked for: ${movie.title}")
                vm.toggleFavorite(movie)
            },
            onDeleteClick = { movie ->
                Log.d("MoviesListFragment", "🗑️ Delete clicked for: ${movie.title}")
                vm.delete(movie)
            }
        )

        b.recyclerView.adapter = adapter
        Log.d("MoviesListFragment", "✅ RecyclerView setup completed")
    }

    private fun setupSearch() {
        b.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = true.also {
                handleSearchQuery(q)
            }
            override fun onQueryTextChange(t: String?) = true.also {
                handleSearchQuery(t)
            }
        })
    }

    private fun handleSearchQuery(query: String?) {
        if (query.isNullOrBlank()) {
            vm.clearFilters()
            if (::clearChip.isInitialized) {
                clearChip.isChecked = true
            }
        } else {
            vm.searchMovies(query)
            if (::clearChip.isInitialized) {
                clearChip.isChecked = false
            }
        }
    }

    private fun setupGenreChips() {
        clearChip = Chip(requireContext()).apply {
            text = getString(R.string.clear_filters)
            isCheckable = true
            isChecked = true
            setChipBackgroundColorResource(android.R.color.holo_blue_light)
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            setOnClickListener {
                b.searchView.setQuery("", false)
                vm.clearFilters()

                for (i in 0 until b.genreChipGroup.childCount) {
                    val chipView = b.genreChipGroup.getChildAt(i) as? Chip
                    if (chipView != null && chipView != this) {
                        chipView.isChecked = false
                    }
                }

                isChecked = true
                adapter.submitList(vm.allMovies.value)
                Toast.makeText(requireContext(), getString(R.string.filters_cleared), Toast.LENGTH_SHORT).show()
            }
        }
        b.genreChipGroup.addView(clearChip)

        vm.allGenres.observe(viewLifecycleOwner) { genres ->
            if (b.genreChipGroup.childCount > 1) {
                b.genreChipGroup.removeViews(1, b.genreChipGroup.childCount - 1)
            }

            genres.filterNotNull().forEach { g ->
                val chip = Chip(requireContext()).apply {
                    text = g
                    isCheckable = true
                    setChipBackgroundColorResource(android.R.color.holo_orange_light)
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    setOnClickListener {
                        b.searchView.setQuery("", false)
                        vm.filterByGenre(g)
                        clearChip.isChecked = false

                        for (i in 0 until b.genreChipGroup.childCount) {
                            val otherChip = b.genreChipGroup.getChildAt(i) as? Chip
                            if (otherChip != null && otherChip != this) {
                                otherChip.isChecked = false
                            }
                        }
                    }
                }
                b.genreChipGroup.addView(chip)
            }
        }
    }

    private fun observeData() {
        vm.allMovies.observe(viewLifecycleOwner) { list ->
            Log.d("MoviesListFragment", "📋 Movies updated: ${list.size} items")
            if (vm.currentFilter.value.isNullOrEmpty()) {
                adapter.submitList(list)
                updateEmpty(list.isEmpty())
            }
        }

        vm.searchResults.observe(viewLifecycleOwner) { list ->
            if (!vm.currentFilter.value.isNullOrEmpty()) {
                adapter.submitList(list)
                updateEmpty(list.isEmpty())
            }
        }

        vm.currentFilter.observe(viewLifecycleOwner) { filter ->
            if (::clearChip.isInitialized) {
                clearChip.isChecked = filter.isNullOrEmpty()
            }
        }
    }

    private fun updateEmpty(empty: Boolean) {
        b.recyclerView.isVisible = !empty
        b.tvEmptyState.isVisible = empty
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}