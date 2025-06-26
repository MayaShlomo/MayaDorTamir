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

class MoviesListFragment : Fragment() {
    private var _b: FragmentMoviesListBinding? = null
    private val b get() = _b!!
    private val vm: MovieViewModel by activityViewModels()
    private lateinit var clearChip: Chip

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        FragmentMoviesListBinding.inflate(i, c, false).also { _b = it }.root

    override fun onViewCreated(v: View, s: Bundle?) {
        setupRecycler()
        setupSearch()
        setupGenreChips()
        observeData()

        b.fabAdd.setOnClickListener {
            val act = MoviesListFragmentDirections.actionMoviesListToAddEditMovie(0)
            findNavController().navigate(act)
        }
    }

    private fun setupRecycler() {
        b.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerView.adapter = MovieAdapter(vm)
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

                (b.recyclerView.adapter as? MovieAdapter)?.submitList(vm.allMovies.value)

                Toast.makeText(requireContext(), "הפילטרים אופסו", Toast.LENGTH_SHORT).show()
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
                        // Clear search text when genre filter is applied
                        b.searchView.setQuery("", false)

                        // Apply the genre filter
                        vm.filterByGenre(g)

                        // Update chip states
                        clearChip.isChecked = false

                        // Uncheck all other genre chips
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
            if (vm.currentFilter.value.isNullOrEmpty()) {
                (b.recyclerView.adapter as MovieAdapter).submitList(list)
                updateEmpty(list.isEmpty())
            }
        }

        vm.searchResults.observe(viewLifecycleOwner) { list ->
            if (!vm.currentFilter.value.isNullOrEmpty()) {
                (b.recyclerView.adapter as MovieAdapter).submitList(list)
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