package com.example.mycinema.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.mycinema.R
import com.example.mycinema.databinding.ItemMovieBinding
import com.example.mycinema.models.Movie
import com.example.mycinema.util.ImageHelper

class MovieAdapter(
    private val onMovieClick: (Movie) -> Unit,  // *** הוספתי את זה ***
    private val onFavoriteClick: (Movie) -> Unit,
    private val onDeleteClick: (Movie) -> Unit
) : ListAdapter<Movie, MovieAdapter.VH>(Diff()) {

    init {
        Log.d("MovieAdapter", "Adapter created")
    }

    inner class VH(private val b: ItemMovieBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(m: Movie) = with(b) {
            Log.d("MovieAdapter", "Binding movie: ${m.id} - ${m.title} - isFavorite: ${m.isFavorite}")

            // מילוי פרטי הסרט
            title.text = m.title

            val genreText = m.genre ?: root.context.getString(R.string.unknown)
            genre.text = root.context.getString(R.string.genre_format, genreText)

            val yearText = m.year?.toString() ?: root.context.getString(R.string.unknown)
            releaseDate.text = root.context.getString(R.string.year_format, yearText)

            val ratingText = m.rating?.toString() ?: root.context.getString(R.string.not_available)
            rating.text = root.context.getString(R.string.rating_format, ratingText)

            // עדכון כפתור מועדפים
            updateFavoriteButton(m.isFavorite)

            // טעינת תמונה
            loadMovieImage(m, imageView)

            // *** תיקון הניווט - עכשיו יעבוד נכון! ***
            root.setOnClickListener {
                Log.d("MovieAdapter", "Movie clicked: ${m.id} - ${m.title}")
                onMovieClick(m)  // קוראים לפונקציה שהועברה מהפרגמנט
            }

            // אם יש כפתור פרטים נפרד
            btnDetails?.setOnClickListener {
                Log.d("MovieAdapter", "Details button clicked for movie: ${m.id} - ${m.title}")
                onMovieClick(m)
            }

            // כפתורי פעולות
            btnFavorites.setOnClickListener {
                Log.d("MovieAdapter", "Favorite button clicked for: ${m.title}")
                onFavoriteClick(m)
            }

            btnDelete.setOnClickListener {
                Log.d("MovieAdapter", "Delete button clicked for: ${m.title}")
                confirmDelete(m)
            }

            root.setOnLongClickListener {
                confirmDelete(m)
                true
            }
        }

        private fun updateFavoriteButton(isFavorite: Boolean) {
            val iconRes = if (isFavorite) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
            b.btnFavorites.setIconResource(iconRes)

            // עדכון צבע הכפתור
            val backgroundTint = if (isFavorite)
                b.root.context.getColor(R.color.warning_orange)
            else
                b.root.context.getColor(R.color.gray_600)
            b.btnFavorites.backgroundTintList = android.content.res.ColorStateList.valueOf(backgroundTint)
        }

        private fun loadMovieImage(movie: Movie, imageView: ImageView) {
            // בדיקה אם יש imageUri ואם זה URL
            if (movie.imageUri != null && (movie.imageUri.startsWith("http://") || movie.imageUri.startsWith("https://"))) {
                // טעינה עם Glide מ-URL
                Glide.with(imageView.context)
                    .load(movie.imageUri)
                    .placeholder(R.drawable.default_movie)
                    .error(R.drawable.default_movie)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(imageView)
            } else {
                // נסיון לטעון מקומית
                ImageHelper.loadMovieImageByTitle(imageView.context, movie.title, imageView)
            }
        }

        private fun confirmDelete(m: Movie) {
            AlertDialog.Builder(b.root.context)
                .setTitle("מחק סרט")
                .setMessage("האם אתה בטוח שברצונך למחוק את הסרט '${m.title}'?")
                .setPositiveButton("מחק") { _, _ ->
                    onDeleteClick(m)
                }
                .setNegativeButton("ביטול", null)
                .show()
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH {
        Log.d("MovieAdapter", "Creating new ViewHolder")
        return VH(ItemMovieBinding.inflate(LayoutInflater.from(p.context), p, false))
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)
        Log.d("MovieAdapter", "Binding item at position $pos: ${item.id} - ${item.title}")
        h.bind(item)
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
        if (payloads.isNotEmpty() && payloads[0] == "FAVORITE_CHANGED") {
            // עדכן רק את הכוכבית
            val movie = getItem(position)
            holder.itemView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnFavorites)?.let { btn ->
                val iconRes = if (movie.isFavorite) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
                btn.setIconResource(iconRes)

                val backgroundTint = if (movie.isFavorite)
                    holder.itemView.context.getColor(R.color.warning_orange)
                else
                    holder.itemView.context.getColor(R.color.gray_600)
                btn.backgroundTintList = android.content.res.ColorStateList.valueOf(backgroundTint)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    class Diff : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(o: Movie, n: Movie): Boolean {
            val result = o.id == n.id
            Log.d("MovieAdapter.Diff", "areItemsTheSame: ${o.id} == ${n.id} = $result")
            return result
        }

        override fun areContentsTheSame(o: Movie, n: Movie): Boolean {
            val result = o.id == n.id &&
                    o.title == n.title &&
                    o.isFavorite == n.isFavorite &&
                    o.description == n.description &&
                    o.genre == n.genre &&
                    o.year == n.year &&
                    o.rating == n.rating &&
                    o.imageUri == n.imageUri

            Log.d("MovieAdapter.Diff", "areContentsTheSame: ${o.id} (fav=${o.isFavorite}) vs ${n.id} (fav=${n.isFavorite}) = $result")
            return result
        }

        override fun getChangePayload(oldItem: Movie, newItem: Movie): Any? {
            if (oldItem.id == newItem.id && oldItem.isFavorite != newItem.isFavorite) {
                return "FAVORITE_CHANGED"
            }
            return super.getChangePayload(oldItem, newItem)
        }
    }
}