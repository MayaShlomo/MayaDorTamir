package com.example.mycinema.fragments

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mycinema.databinding.FragmentAddEditMovieBinding
import com.example.mycinema.models.Movie
import com.example.mycinema.viewmodel.MovieViewModel

class AddEditMovieFragment : Fragment() {
    private var _b: FragmentAddEditMovieBinding? = null
    private val b get() = _b!!
    private val vm: MovieViewModel by viewModels()
    private val args: AddEditMovieFragmentArgs by navArgs()
    private var imageUri: Uri? = null

    private val pickImg = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let { uri -> imageUri = uri; b.ivPoster.setImageURI(uri) }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAddEditMovieBinding.inflate(i, c, false)
        return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        if (args.movieId != 0)
            vm.get(args.movieId).observe(viewLifecycleOwner) { bind(it) }

        b.btnChooseImage.setOnClickListener { pickImg.launch("image/*") }
        b.btnSave.setOnClickListener { save() }
    }

    private fun bind(m: Movie) = with(b) {
        etTitle.setText(m.title)
        etDesc.setText(m.description)
        etGenre.setText(m.genre ?: "")
        etActors.setText(m.actors ?: "")
        etDirector.setText(m.director ?: "")
        etYear.setText(m.year?.toString() ?: "")
        etRating.setText(m.rating?.toString() ?: "")
        etShowtime.setText(m.showtime)
        etReleaseDate.setText(m.releaseDate ?: "")
        etDuration.setText(m.duration?.toString() ?: "")
        imageUri = m.imageUri?.toUri()
        ivPoster.setImageURI(imageUri)
    }

    private fun save() {
        vm.insert(
            Movie(
                id = args.movieId,
                title = b.etTitle.text.toString(),
                description = b.etDesc.text.toString(),
                genre = b.etGenre.text.toString(),
                actors = b.etActors.text.toString(),
                director = b.etDirector.text.toString(),
                year = b.etYear.text.toString().toIntOrNull(),
                rating = b.etRating.text.toString().toFloatOrNull(),
                imageUri = imageUri?.toString(),
                showtime = b.etShowtime.text.toString(),
                isFavorite = false,
                releaseDate = b.etReleaseDate.text.toString(),
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