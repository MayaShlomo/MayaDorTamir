// CinemaDetailsFragment.kt - תיקון ניווט כפול
package com.example.mycinema.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.mycinema.R
import com.example.mycinema.adapter.ShowtimeAdapter
import com.example.mycinema.databinding.FragmentCinemaDetailsBinding
import com.example.mycinema.models.Cinema
import com.example.mycinema.models.CinemaWithShowtimes
import com.example.mycinema.viewmodel.CinemaViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CinemaDetailsFragment : Fragment() {

    private var _binding: FragmentCinemaDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: CinemaDetailsFragmentArgs by navArgs()
    private val cinemaViewModel: CinemaViewModel by activityViewModels()
    private lateinit var showtimeAdapter: ShowtimeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCinemaDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("CinemaDetailsFragment", "Loading details for cinema: ${args.cinemaId}")

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // טעינת פרטי בית הקולנוע
        cinemaViewModel.loadCinemaDetails(args.cinemaId)
    }

    private fun setupRecyclerView() {
        showtimeAdapter = ShowtimeAdapter { showtime ->
            // לחיצה על הקרנה - אפשר להוסיף פונקציונליות הזמנה
            showBookingDialog(showtime.movieTitle, showtime.showTime, showtime.price)
        }

        binding.recyclerViewShowtimes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = showtimeAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnNavigate.setOnClickListener {
            cinemaViewModel.selectedCinema.value?.cinema?.let { cinema ->
                openNavigation(cinema)
            }
        }

        binding.btnCall.setOnClickListener {
            cinemaViewModel.selectedCinema.value?.cinema?.phone?.let { phone ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                startActivity(intent)
            }
        }

        binding.btnWebsite.setOnClickListener {
            cinemaViewModel.selectedCinema.value?.cinema?.website?.let { website ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(website))
                startActivity(intent)
            }
        }
    }

    private fun observeViewModel() {
        cinemaViewModel.selectedCinema.observe(viewLifecycleOwner) { cinemaWithShowtimes ->
            cinemaWithShowtimes?.let {
                displayCinemaDetails(it)
            }
        }

        cinemaViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        cinemaViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                showError(errorMessage)
                cinemaViewModel.clearError()
            }
        }
    }

    private fun displayCinemaDetails(cinemaWithShowtimes: CinemaWithShowtimes) {
        val cinema = cinemaWithShowtimes.cinema
        val showtimes = cinemaWithShowtimes.showtimes

        Log.d("CinemaDetailsFragment", "Displaying details for: ${cinema.name}")

        // פרטי בית הקולנוע
        binding.tvCinemaName.text = cinema.name
        binding.tvCinemaAddress.text = cinema.address

        if (cinema.rating > 0) {
            binding.ratingBar.rating = cinema.rating
            binding.tvRating.text = String.format("%.1f", cinema.rating)
            binding.layoutRating.isVisible = true
        } else {
            binding.layoutRating.isVisible = false
        }

        // פרטי קשר
        binding.btnCall.isVisible = !cinema.phone.isNullOrBlank()
        binding.btnWebsite.isVisible = !cinema.website.isNullOrBlank()

        // תמונת בית הקולנוע
        if (!cinema.imageUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(cinema.imageUrl)
                .placeholder(R.drawable.ic_cinema)
                .error(R.drawable.ic_cinema)
                .into(binding.ivCinemaImage)
        } else {
            binding.ivCinemaImage.setImageResource(R.drawable.ic_cinema)
        }

        // הקרנות
        if (showtimes.isNotEmpty()) {
            binding.layoutShowtimes.isVisible = true
            binding.layoutNoShowtimes.isVisible = false
            binding.tvShowtimeCount.text = getString(R.string.showtimes_count, showtimes.size)
            showtimeAdapter.submitList(showtimes)
        } else {
            binding.layoutShowtimes.isVisible = false
            binding.layoutNoShowtimes.isVisible = true
        }
    }

    private fun openNavigation(cinema: Cinema) {
        val uri = Uri.parse("geo:${cinema.latitude},${cinema.longitude}?q=${cinema.latitude},${cinema.longitude}(${cinema.name})")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            val webUri = Uri.parse("https://maps.google.com/?q=${cinema.latitude},${cinema.longitude}")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            startActivity(webIntent)
        }
    }

    private fun showBookingDialog(movieTitle: String, showTime: String, price: Float?) {
        val message = buildString {
            append("${getString(R.string.movie)}: $movieTitle\n")
            append("${getString(R.string.time)}: $showTime\n")
            price?.let {
                append("${getString(R.string.price)}: ${String.format("%.0f", it)}₪")
            }
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.booking_info))
            .setMessage(message)
            .setPositiveButton(getString(R.string.call_to_book)) { _, _ ->
                cinemaViewModel.selectedCinema.value?.cinema?.phone?.let { phone ->
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phone")
                    }
                    startActivity(intent)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
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