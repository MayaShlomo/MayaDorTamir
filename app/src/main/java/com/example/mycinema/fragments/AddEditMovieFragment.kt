package com.example.mycinema.fragments

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mycinema.R
import com.example.mycinema.databinding.FragmentAddEditMovieBinding
import com.example.mycinema.models.Movie
import com.example.mycinema.viewmodel.MovieViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
@AndroidEntryPoint
class AddEditMovieFragment : Fragment() {


    private var _b: FragmentAddEditMovieBinding? = null
    private val b get() = _b!!
    private val vm: MovieViewModel by viewModels()
    private val args: AddEditMovieFragmentArgs by navArgs()

    // הוסרנו את imageUri מה-Fragment - זה יישמר ב-ViewModel
    private var selectedGenre: String? = null
    private var selectedYear: Int? = null
    private var selectedDate: String? = null
    private var selectedRating: Float? = null

    private val pickImg = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri ->
            b.ivPoster.setImageURI(uri)
            vm.setImageUri(uri.toString())
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAddEditMovieBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        setupUI()

        if (args.movieId != 0) {
            vm.get(args.movieId).observe(viewLifecycleOwner) { bind(it) }
            b.tvAddEditTitle.text = getString(R.string.edit_movie_title)
        } else {
            b.tvAddEditTitle.text = getString(R.string.add_movie_title)
        }

        b.btnChooseImage.setOnClickListener { pickImg.launch("image/*") }
        b.btnSave.setOnClickListener { save() }
    }

    private fun setupUI() {
        setupGenreSpinner()
        setupDatePicker()
        setupRatingBar()
        setupYearPicker()
    }

    private fun setupGenreSpinner() {
        val genres = resources.getStringArray(R.array.movie_genres)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        b.spinnerGenre.adapter = adapter

        b.spinnerGenre.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedGenre = genres[position]
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun setupDatePicker() {
        b.btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val date = String.format("%04d-%02d-%02d", year, month + 1, day)
                    b.tvSelectedDate.text = date
                    selectedDate = date
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupRatingBar() {
        b.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            val ratingValue = rating * 2 // Convert from 0-5 to 0-10 scale
            b.tvRatingValue.text = String.format("%.1f", ratingValue)
            selectedRating = ratingValue
        }
    }

    private fun setupYearPicker() {
        b.btnSelectYear.setOnClickListener {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val years = (1900..currentYear + 5).map { it.toString() }.toTypedArray()

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_year))
                .setItems(years) { _, which ->
                    val year = years[which]
                    b.tvSelectedYear.text = year
                    selectedYear = year.toInt()
                }
                .show()
        }
    }

    private fun bind(m: Movie) = with(b) {
        etTitle.setText(m.title)
        etDesc.setText(m.description)
        etActors.setText(m.actors ?: "")
        etDirector.setText(m.director ?: "")
        etShowtime.setText(m.showtime)
        etDuration.setText(m.duration?.toString() ?: "")

        // הגדרת ז'אנר ב-Spinner
        m.genre?.let { genre ->
            val genres = resources.getStringArray(R.array.movie_genres)
            val position = genres.indexOf(genre)
            if (position >= 0) {
                spinnerGenre.setSelection(position)
                selectedGenre = genre
            }
        }

        // הגדרת שנה
        m.year?.let { year ->
            tvSelectedYear.text = year.toString()
            selectedYear = year
        }

        // הגדרת תאריך
        m.releaseDate?.let { date ->
            tvSelectedDate.text = date
            selectedDate = date
        }

        // הגדרת דירוג
        m.rating?.let { rating ->
            val ratingBarValue = rating / 2f // Convert from 0-10 to 0-5 scale
            ratingBar.rating = ratingBarValue
            tvRatingValue.text = String.format("%.1f", rating)
            selectedRating = rating
        }

        // הגדרת תמונה
        m.imageUri?.let { uri ->
            try {
                ivPoster.setImageURI(uri.toUri())
                vm.setImageUri(uri)
            } catch (e: Exception) {
                // Handle error loading image
            }
        }
    }

    private fun save() {
        val title = b.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            b.etTitle.error = getString(R.string.movie_title_label)
            return
        }

        val description = b.etDesc.text.toString().trim()
        if (description.isEmpty()) {
            b.etDesc.error = getString(R.string.description_label)
            return
        }

        vm.insert(
            Movie(
                id = args.movieId,
                title = title,
                description = description,
                genre = selectedGenre,
                actors = b.etActors.text.toString().takeIf { it.isNotBlank() },
                director = b.etDirector.text.toString().takeIf { it.isNotBlank() },
                year = selectedYear,
                rating = selectedRating,
                imageUri = vm.getImageUri(),
                showtime = b.etShowtime.text.toString().takeIf { it.isNotBlank() } ?: "20:00",
                isFavorite = false,
                releaseDate = selectedDate,
                duration = b.etDuration.text.toString().toIntOrNull()
            )
        )
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}