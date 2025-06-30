package com.example.mycinema.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
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
    private val vm: MovieViewModel by activityViewModels()
    private val args: AddEditMovieFragmentArgs by navArgs()

    // נתונים שהגיעו מ-API (אם יש)
    private var apiImageUrl: String? = null
    private var apiId: Int? = null
    private var isViewOnlyMode = false

    private val pickImg = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri ->
            if (!isViewOnlyMode) {
                b.ivPoster.setImageURI(uri)
                vm.setImageUri(uri.toString())
                apiImageUrl = null // משתמש העלה תמונה משלו
            }
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAddEditMovieBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        setupUI()
        observeViewModel()

        // בדיקה אם הגענו מחיפוש API
        arguments?.let { bundle ->
            isViewOnlyMode = bundle.getBoolean("viewOnly", false)

            if (bundle.containsKey("apiTitle")) {
                loadApiData(bundle)

                if (isViewOnlyMode) {
                    b.tvAddEditTitle.text = "פרטי הסרט מ-TMDb"
                    setupViewOnlyMode()
                } else {
                    b.tvAddEditTitle.text = getString(R.string.add_movie_from_tmdb)
                }
            }
        }

        if (args.movieId != 0) {
            vm.get(args.movieId).observe(viewLifecycleOwner) { bind(it) }
            b.tvAddEditTitle.text = getString(R.string.edit_movie_title)
        } else if (!arguments?.containsKey("apiTitle")!!) {
            b.tvAddEditTitle.text = getString(R.string.add_movie_title)
            vm.clearMovieFormData()
        }

        b.btnChooseImage.setOnClickListener {
            if (!isViewOnlyMode) pickImg.launch("image/*")
        }

        b.btnSave.setOnClickListener {
            if (isViewOnlyMode) {
                // במצב צפייה - הוסף לאוסף
                addToCollection()
            } else {
                save()
            }
        }
    }

    private fun setupViewOnlyMode() {
        // הפוך את כל השדות ללא-ניתנים לעריכה
        b.etTitle.isEnabled = false
        b.etDesc.isEnabled = false
        b.etActors.isEnabled = false
        b.etDirector.isEnabled = false
        b.etShowtime.isEnabled = false
        b.etDuration.isEnabled = false
        b.spinnerGenre.isEnabled = false
        b.btnSelectYear.isEnabled = false
        b.btnSelectDate.isEnabled = false
        b.ratingBar.isEnabled = false
        b.btnChooseImage.isVisible = false

        // שנה את טקסט הכפתור
        b.btnSave.text = "הוסף לאוסף"
        b.btnSave.setBackgroundColor(resources.getColor(R.color.success_green, null))

        // הוסף כפתור "עריכה וגמירה"
        b.btnSave.layoutParams.width = 0
        // ניתן להוסיף כפתור נוסף או לשנות את הפונקציונליות
    }

    private fun addToCollection() {
        arguments?.let { bundle ->
            val apiTitle = bundle.getString("apiTitle") ?: return
            val apiDescription = bundle.getString("apiDescription") ?: ""
            val apiImageUrl = bundle.getString("apiImageUrl")
            val apiReleaseDate = bundle.getString("apiReleaseDate")
            val apiRating = bundle.getFloat("apiRating", 0f)
            val apiId = bundle.getInt("apiId", 0)
            val apiGenres = bundle.getString("apiGenres")

            val formData = vm.getMovieFormData()

            vm.insert(
                Movie(
                    id = 0,
                    apiId = if (apiId > 0) apiId else null,
                    title = apiTitle,
                    description = apiDescription,
                    genre = apiGenres?.split(",")?.firstOrNull()?.trim(),
                    actors = null,
                    director = null,
                    year = apiReleaseDate?.substring(0, 4)?.toIntOrNull(),
                    rating = apiRating,
                    imageUri = apiImageUrl,
                    showtime = "20:00",
                    isFavorite = false,
                    releaseDate = apiReleaseDate,
                    duration = null,
                    isFromApi = true
                )
            )

            // הצגת הודעה והחזרה
            AlertDialog.Builder(requireContext())
                .setTitle("הסרט נוסף!")
                .setMessage("הסרט '$apiTitle' נוסף בהצלחה לאוסף שלך")
                .setPositiveButton("אישור") { _, _ ->
                    findNavController().navigateUp()
                }
                .show()
        }
    }

    private fun loadApiData(bundle: Bundle) {
        // טעינת נתונים שהגיעו מה-API
        bundle.getString("apiTitle")?.let { b.etTitle.setText(it) }
        bundle.getString("apiDescription")?.let { b.etDesc.setText(it) }

        bundle.getString("apiImageUrl")?.let { url ->
            apiImageUrl = url
            // טעינת תמונה עם Glide
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.default_movie)
                .error(R.drawable.default_movie)
                .into(b.ivPoster)
        }

        bundle.getString("apiReleaseDate")?.let { date ->
            b.tvSelectedDate.text = date
            vm.setSelectedDate(date)
        }

        bundle.getFloat("apiRating", 0f).let { rating ->
            if (rating > 0) {
                vm.setSelectedRating(rating)
                b.ratingBar.rating = rating / 2f
                b.tvRatingValue.text = String.format("%.1f", rating)
            }
        }

        bundle.getInt("apiId", 0).let { id ->
            if (id > 0) apiId = id
        }

        bundle.getString("apiGenres")?.let { genres ->
            vm.setSelectedGenre(genres.split(",").firstOrNull()?.trim())
        }

        // הוספת הערה שזה סרט מ-TMDB
        b.tvSourceNote.visibility = View.VISIBLE
        b.tvSourceNote.text = getString(R.string.movie_from_tmdb)
    }

    private fun setupUI() {
        setupGenreSpinner()
        setupDatePicker()
        setupRatingBar()
        setupYearPicker()
    }

    private fun observeViewModel() {
        vm.movieFormData.observe(viewLifecycleOwner) { formData ->
            formData.imageUri?.let { uri ->
                if (apiImageUrl == null && !isViewOnlyMode) { // רק אם לא טענו כבר מ-API ולא במצב צפייה
                    try {
                        b.ivPoster.setImageURI(uri.toUri())
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            }

            formData.selectedGenre?.let { genre ->
                val genres = resources.getStringArray(R.array.movie_genres)
                val position = genres.indexOf(genre)
                if (position >= 0) {
                    b.spinnerGenre.setSelection(position)
                }
            }

            formData.selectedYear?.let { year ->
                b.tvSelectedYear.text = year.toString()
            }

            formData.selectedDate?.let { date ->
                b.tvSelectedDate.text = date
            }

            formData.selectedRating?.let { rating ->
                val ratingBarValue = rating / 2f
                b.ratingBar.rating = ratingBarValue
                b.tvRatingValue.text = String.format("%.1f", rating)
            }
        }
    }

    private fun setupGenreSpinner() {
        val genres = resources.getStringArray(R.array.movie_genres)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        b.spinnerGenre.adapter = adapter

        b.spinnerGenre.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (!isViewOnlyMode) vm.setSelectedGenre(genres[position])
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun setupDatePicker() {
        b.btnSelectDate.setOnClickListener {
            if (isViewOnlyMode) return@setOnClickListener

            val calendar = Calendar.getInstance()

            // אם יש תאריך מה-API, נתחיל ממנו
            b.tvSelectedDate.text?.toString()?.let { dateStr ->
                try {
                    val parts = dateStr.split("-")
                    if (parts.size == 3) {
                        calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val date = String.format("%04d-%02d-%02d", year, month + 1, day)
                    b.tvSelectedDate.text = date
                    vm.setSelectedDate(date)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupRatingBar() {
        b.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            if (!isViewOnlyMode) {
                val ratingValue = rating * 2
                b.tvRatingValue.text = String.format("%.1f", ratingValue)
                vm.setSelectedRating(ratingValue)
            }
        }
    }

    private fun setupYearPicker() {
        b.btnSelectYear.setOnClickListener {
            if (isViewOnlyMode) return@setOnClickListener

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val years = (1900..currentYear + 5).map { it.toString() }.toTypedArray()

            // אם יש כבר שנה מתאריך השחרור, נבחר אותה
            val selectedYear = b.tvSelectedDate.text?.toString()?.substring(0, 4)
            val defaultSelection = years.indexOf(selectedYear).takeIf { it >= 0 } ?: years.size - 1

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_year))
                .setSingleChoiceItems(years, defaultSelection) { dialog, which ->
                    val year = years[which]
                    b.tvSelectedYear.text = year
                    vm.setSelectedYear(year.toInt())
                    dialog.dismiss()
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

        m.genre?.let { vm.setSelectedGenre(it) }
        m.year?.let { vm.setSelectedYear(it) }
        m.releaseDate?.let { vm.setSelectedDate(it) }
        m.rating?.let { vm.setSelectedRating(it) }

        // טעינת תמונה - בדיקה אם זה URL או URI מקומי
        m.imageUri?.let { imageUri ->
            if (imageUri.startsWith("http")) {
                apiImageUrl = imageUri
                Glide.with(this@AddEditMovieFragment)
                    .load(imageUri)
                    .placeholder(R.drawable.default_movie)
                    .into(ivPoster)
            } else {
                vm.setImageUri(imageUri)
            }
        }

        // הצגת מקור אם זה מ-API
        if (m.isFromApi) {
            tvSourceNote.visibility = View.VISIBLE
            tvSourceNote.text = getString(R.string.movie_from_tmdb)
        }

        apiId = m.apiId
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

        val formData = vm.getMovieFormData()

        // החלטה איזו תמונה לשמור
        val finalImageUri = when {
            formData.imageUri != null -> formData.imageUri // המשתמש העלה תמונה
            apiImageUrl != null -> apiImageUrl // יש תמונה מ-API
            else -> null
        }

        vm.insert(
            Movie(
                id = args.movieId,
                apiId = apiId,
                title = title,
                description = description,
                genre = formData.selectedGenre,
                actors = b.etActors.text.toString().takeIf { it.isNotBlank() },
                director = b.etDirector.text.toString().takeIf { it.isNotBlank() },
                year = formData.selectedYear,
                rating = formData.selectedRating,
                imageUri = finalImageUri,
                showtime = b.etShowtime.text.toString().takeIf { it.isNotBlank() } ?: "20:00",
                isFavorite = false,
                releaseDate = formData.selectedDate,
                duration = b.etDuration.text.toString().toIntOrNull(),
                isFromApi = apiId != null
            )
        )
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}