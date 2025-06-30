// CinemaShowingMovieAdapter.kt - מתוקן
package com.example.mycinema.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mycinema.R
import com.example.mycinema.databinding.ItemCinemaShowingMovieBinding
import com.example.mycinema.models.Cinema
import com.example.mycinema.models.CinemaWithDistance

class CinemaShowingMovieAdapter(
    private val onCinemaClick: (Cinema) -> Unit,
    private val onNavigateClick: (Cinema) -> Unit,
    private val onViewShowtimesClick: (Cinema) -> Unit
) : ListAdapter<CinemaWithDistance, CinemaShowingMovieAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemCinemaShowingMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cinemaWithDistance: CinemaWithDistance) = with(binding) {
            val cinema = cinemaWithDistance.cinema
            val distance = cinemaWithDistance.distance

            // פרטי בית הקולנוע
            tvCinemaName.text = cinema.name
            tvCinemaAddress.text = cinema.address

            // מרחק - תיקון הבעיה
            if (distance > 0) {
                // שימוש פשוט ב-String.format במקום getString מסובך
                tvDistance.text = String.format("%.1f km away", distance)
                tvDistance.isVisible = true
            } else {
                tvDistance.isVisible = false
            }

            // דירוג
            if (cinema.rating > 0) {
                ratingBar.rating = cinema.rating
                tvRatingValue.text = String.format("%.1f", cinema.rating)
                layoutRating.isVisible = true
            } else {
                layoutRating.isVisible = false
            }

            // תמונה
            Glide.with(ivCinemaImage.context)
                .load(cinema.imageUrl)
                .placeholder(R.drawable.ic_cinema)
                .error(R.drawable.ic_cinema)
                .centerCrop()
                .into(ivCinemaImage)

            // לחיצות
            root.setOnClickListener { onCinemaClick(cinema) }
            btnNavigate.setOnClickListener { onNavigateClick(cinema) }
            btnViewShowtimes.setOnClickListener { onViewShowtimesClick(cinema) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCinemaShowingMovieBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<CinemaWithDistance>() {
        override fun areItemsTheSame(
            oldItem: CinemaWithDistance,
            newItem: CinemaWithDistance
        ): Boolean {
            return oldItem.cinema.id == newItem.cinema.id
        }

        override fun areContentsTheSame(
            oldItem: CinemaWithDistance,
            newItem: CinemaWithDistance
        ): Boolean {
            return oldItem == newItem
        }
    }
}