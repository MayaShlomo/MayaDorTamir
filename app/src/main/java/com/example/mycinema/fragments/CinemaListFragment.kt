// CinemaListFragment.kt
// החלף את: app/src/main/java/com/example/mycinema/fragments/CinemaListFragment.kt
package com.example.mycinema.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycinema.R
import com.example.mycinema.adapter.CinemaAdapter
import com.example.mycinema.databinding.FragmentCinemaListBinding
import com.example.mycinema.viewmodel.CinemaViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CinemaListFragment : Fragment() {

    private var _binding: FragmentCinemaListBinding? = null
    private val binding get() = _binding!!

    private val cinemaViewModel: CinemaViewModel by activityViewModels()
    private lateinit var adapter: CinemaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCinemaListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("CinemaListFragment", "Fragment created")

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // *** שינוי עיקרי - טען תמיד משהו! ***
        loadCinemasWithFallback()
    }

    private fun loadCinemasWithFallback() {
        // נסה לטעון מקומיים, אם לא - טען מפורסמים
        val currentCinemas = cinemaViewModel.nearbyCinemas.value

        if (currentCinemas.isNullOrEmpty()) {
            Log.d("CinemaListFragment", "No current cinemas, trying fallback")
            cinemaViewModel.loadAnyCinemas() // הפונקציה החדשה
        } else {
            Log.d("CinemaListFragment", "Already have ${currentCinemas.size} cinemas")
        }
    }

    private fun setupRecyclerView() {
        adapter = CinemaAdapter(
            onCinemaClick = { cinema ->
                navigateToCinemaDetails(cinema.id)
            },
            onNavigateClick = { cinema ->
                // פתיחת ניווט ל-Google Maps
                val uri = android.net.Uri.parse(
                    "geo:${cinema.latitude},${cinema.longitude}?q=${cinema.latitude},${cinema.longitude}(${cinema.name})"
                )
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")

                if (intent.resolveActivity(requireActivity().packageManager) != null) {
                    startActivity(intent)
                } else {
                    // אם אין Google Maps
                    val webUri = android.net.Uri.parse(
                        "https://maps.google.com/?q=${cinema.latitude},${cinema.longitude}"
                    )
                    val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, webUri)
                    startActivity(webIntent)
                }
            },
            onCallClick = { cinema ->
                cinema.phone?.let { phone ->
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                        data = android.net.Uri.parse("tel:$phone")
                    }
                    startActivity(intent)
                }
            }
        )

        binding.recyclerViewCinemas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CinemaListFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.btnMapView.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnRefresh.setOnClickListener {
            cinemaViewModel.loadAnyCinemas() // *** שימוש בפונקציה החדשה ***
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            cinemaViewModel.refresh()
        }
    }

    private fun observeViewModel() {
        // בתי קולנוע בקרבת מקום
        cinemaViewModel.nearbyCinemas.observe(viewLifecycleOwner) { cinemas ->
            Log.d("CinemaListFragment", "Received ${cinemas.size} nearby cinemas")

            adapter.submitList(cinemas)
            updateEmptyState(cinemas.isEmpty())

            binding.tvCinemaCount.text = getString(R.string.cinemas_found, cinemas.size)
        }

        // מצב טעינה
        cinemaViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.swipeRefreshLayout.isRefreshing = isLoading && binding.swipeRefreshLayout.isRefreshing

            if (!isLoading) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        // שגיאות
        cinemaViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                showError(errorMessage)
                cinemaViewModel.clearError()
            }
        }

        // הרשאות מיקום
        cinemaViewModel.hasLocationPermission.observe(viewLifecycleOwner) { hasPermission ->
            binding.tvLocationNote.isVisible = !hasPermission
            binding.btnRefresh.isEnabled = true // תמיד אפשר לרענן
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.recyclerViewCinemas.isVisible = !isEmpty
        binding.layoutEmptyState.isVisible = isEmpty

        if (isEmpty) {
            binding.tvEmptyMessage.text = "No cinemas found"
            binding.tvEmptyDescription.text = "Try refreshing or check your connection"
        }
    }

    private fun navigateToCinemaDetails(cinemaId: Int) {
        val action = CinemaListFragmentDirections
            .actionCinemaListToDetails(cinemaId)
        findNavController().navigate(action)
    }

    private fun showError(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}