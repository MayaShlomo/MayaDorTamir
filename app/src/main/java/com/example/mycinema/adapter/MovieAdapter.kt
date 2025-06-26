package com.example.mycinema.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.recyclerview.widget.*
import com.example.mycinema.R
import com.example.mycinema.databinding.ItemMovieBinding
import com.example.mycinema.fragments.FavoritesFragmentDirections
import com.example.mycinema.fragments.MoviesListFragmentDirections
import com.example.mycinema.models.Movie
import com.example.mycinema.util.ImageHelper

// האדפטר מתוקן - ללא ViewModel, רק listeners
class MovieAdapter(
    private val onFavoriteClick: (Movie) -> Unit,
    private val onDeleteClick: (Movie) -> Unit
) : ListAdapter<Movie, MovieAdapter.VH>(Diff()) {

    init {
        Log.d("MovieAdapter", "Adapter created")
    }

    inner class VH(private val b: ItemMovieBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(m: Movie) = with(b) {
            Log.d("MovieAdapter", "Binding movie: ${m.id} - ${m.title} - isFavorite: ${m.isFavorite}")

            title.text = m.title
            genre.text = root.context.getString(R.string.genre_format, m.genre ?: root.context.getString(R.string.unknown))
            releaseDate.text = root.context.getString(R.string.year_format, m.year?.toString() ?: root.context.getString(R.string.unknown))
            rating.text = root.context.getString(R.string.rating_format, m.rating?.toString() ?: root.context.getString(R.string.not_available))

            btnFavorites.setImageResource(
                if (m.isFavorite) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )

            ImageHelper.loadMovieImageByTitle(root.context, m.title, imageView)

            // טיפול בניווט
            btnDetails.setOnClickListener {
                try {
                    val currentDestination = root.findNavController().currentDestination?.id
                    Log.d("MovieAdapter", "Navigation from destination: $currentDestination")

                    when(currentDestination) {
                        R.id.moviesListFragment -> {
                            val action = MoviesListFragmentDirections.actionMoviesListToMovieDetails(m.id)
                            root.findNavController().navigate(action)
                        }
                        R.id.favoritesFragment -> {
                            val action = FavoritesFragmentDirections.actionFavoritesToMovieDetails(m.id)
                            root.findNavController().navigate(action)
                        }
                        else -> {
                            Log.d("MovieAdapter", "Using direct navigation with bundle")
                            val bundle = Bundle()
                            bundle.putInt("movieId", m.id)
                            root.findNavController().navigate(R.id.movieDetailsFragment, bundle)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MovieAdapter", "Navigation error: ${e.message}")
                    try {
                        val bundle = Bundle()
                        bundle.putInt("movieId", m.id)
                        root.findNavController().navigate(R.id.movieDetailsFragment, bundle)
                    } catch (e2: Exception) {
                        Log.e("MovieAdapter", "Fallback navigation error: ${e2.message}")
                    }
                }
            }

            // העברת האירועים החוצה דרך listeners - ללא קשר ל-ViewModel
            btnFavorites.setOnClickListener {
                onFavoriteClick(m)
            }

            btnDelete.setOnClickListener {
                confirmDelete(m)
            }

            root.setOnLongClickListener {
                confirmDelete(m)
                true
            }
        }

        private fun confirmDelete(m: Movie) {
            AlertDialog.Builder(b.root.context)
                .setTitle(b.root.context.getString(R.string.delete_movie_title, m.title))
                .setMessage(b.root.context.getString(R.string.delete_movie_message))
                .setPositiveButton(b.root.context.getString(R.string.delete)) { _, _ ->
                    onDeleteClick(m) // העברת האירוע החוצה
                }
                .setNegativeButton(android.R.string.cancel, null)
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
            holder.itemView.findViewById<ImageView>(R.id.btnFavorites).setImageResource(
                if (movie.isFavorite) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
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