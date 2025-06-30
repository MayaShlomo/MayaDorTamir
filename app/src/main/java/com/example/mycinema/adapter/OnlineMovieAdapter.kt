package com.example.mycinema.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.mycinema.R
import com.example.mycinema.databinding.ItemOnlineMovieBinding
import com.example.mycinema.models.ApiMovie
import com.example.mycinema.models.GenreMapper
import com.example.mycinema.network.MovieApiService

class OnlineMovieAdapter(
    private val onMovieClick: (ApiMovie) -> Unit,
    private val onAddToLocalClick: (ApiMovie) -> Unit
) : ListAdapter<ApiMovie, OnlineMovieAdapter.ViewHolder>(DiffCallback()) {

    init {
        Log.d("OnlineMovieAdapter", "Adapter created")
    }

    inner class ViewHolder(private val binding: ItemOnlineMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(apiMovie: ApiMovie) = with(binding) {
            Log.d("OnlineMovieAdapter", "Binding movie: ${apiMovie.title}")

            // פרטי הסרט
            tvTitle.text = apiMovie.title
            tvOverview.text = apiMovie.overview.takeIf { it.isNotBlank() }
                ?: root.context.getString(R.string.no_description_available)

            // ז'אנרים
            val genres = GenreMapper.getGenreNames(apiMovie.genreIds)
            tvGenres.text = if (genres.isNotBlank()) {
                root.context.getString(R.string.genres_format, genres)
            } else {
                root.context.getString(R.string.genres_format, root.context.getString(R.string.unknown))
            }

            // שנה - תיקון הבעיה
            val year = when {
                apiMovie.releaseDate.isNullOrBlank() -> root.context.getString(R.string.unknown)
                apiMovie.releaseDate.length >= 4 -> apiMovie.releaseDate.substring(0, 4)
                else -> root.context.getString(R.string.unknown)
            }
            tvYear.text = root.context.getString(R.string.year_format, year)

            // דירוג
            tvRating.text = root.context.getString(R.string.rating_format,
                String.format("%.1f", apiMovie.voteAverage))

            // רטינג בר (המרה מ-0-10 ל-0-5)
            ratingBar.rating = apiMovie.voteAverage / 2f

            // טעינת תמונה עם Glide
            loadMoviePoster(apiMovie.posterPath)

            // *** תיקון הלחיצות - פותר את בעיית הדף הלבן! ***
            root.setOnClickListener {
                showMovieDetailsDialog(apiMovie)
            }

            btnAddToCollection.setOnClickListener {
                Log.d("OnlineMovieAdapter", "Add to collection clicked for: ${apiMovie.title}")
                onAddToLocalClick(apiMovie)
            }
        }

        /**
         * פתרון לבעיית הדף הלבן - הצגת דיאלוג במקום ניווט
         */
        private fun showMovieDetailsDialog(apiMovie: ApiMovie) {
            val message = buildString {
                append("📽️ ${apiMovie.title}\n\n")

                // תיאור
                if (apiMovie.overview.isNotBlank()) {
                    append("📝 תיאור:\n${apiMovie.overview}\n\n")
                }

                // שנה
                val year = if (apiMovie.releaseDate.isNullOrBlank()) "לא ידוע"
                else apiMovie.releaseDate.substring(0, 4)
                append("📅 שנה: $year\n")

                // דירוג
                append("⭐ דירוג: ${String.format("%.1f", apiMovie.voteAverage)}/10\n")

                // ז'אנרים
                val genres = GenreMapper.getGenreNames(apiMovie.genreIds)
                if (genres.isNotBlank()) {
                    append("🎬 ז'אנרים: $genres\n")
                }

                append("\n💡 כדי לצפות בפרטים מלאים, הוסף את הסרט לאוסף שלך")
            }

            AlertDialog.Builder(binding.root.context)
                .setTitle("פרטי הסרט")
                .setMessage(message)
                .setPositiveButton("הוסף לאוסף") { _, _ ->
                    onAddToLocalClick(apiMovie)
                }
                .setNeutralButton("סגור", null)
                .create()
                .apply {
                    // עיצוב משופר לדיאלוג
                    setOnShowListener {
                        getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                            context.getColor(R.color.success_green)
                        )
                        getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(
                            context.getColor(R.color.gray_600)
                        )
                    }
                }
                .show()
        }

        private fun loadMoviePoster(posterPath: String?) {
            val imageUrl = MovieApiService.getPosterUrl(posterPath)

            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.default_movie)
                .error(R.drawable.default_movie)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(binding.ivPoster)

            Log.d("OnlineMovieAdapter", "Loading image: $imageUrl")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("OnlineMovieAdapter", "Creating new ViewHolder")
        val binding = ItemOnlineMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = getItem(position)
        Log.d("OnlineMovieAdapter", "Binding item at position $position: ${movie.title}")
        holder.bind(movie)
    }

    class DiffCallback : DiffUtil.ItemCallback<ApiMovie>() {
        override fun areItemsTheSame(oldItem: ApiMovie, newItem: ApiMovie): Boolean {
            val result = oldItem.id == newItem.id
            Log.d("OnlineMovieAdapter.Diff", "areItemsTheSame: ${oldItem.id} == ${newItem.id} = $result")
            return result
        }

        override fun areContentsTheSame(oldItem: ApiMovie, newItem: ApiMovie): Boolean {
            val result = oldItem.id == newItem.id &&
                    oldItem.title == newItem.title &&
                    oldItem.overview == newItem.overview &&
                    oldItem.voteAverage == newItem.voteAverage &&
                    oldItem.posterPath == newItem.posterPath &&
                    oldItem.releaseDate == newItem.releaseDate

            Log.d("OnlineMovieAdapter.Diff", "areContentsTheSame: ${oldItem.title} = $result")
            return result
        }
    }
}