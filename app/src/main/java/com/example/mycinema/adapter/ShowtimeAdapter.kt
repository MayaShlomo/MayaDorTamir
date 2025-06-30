// ShowtimeAdapter.kt
package com.example.mycinema.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mycinema.R
import com.example.mycinema.databinding.ItemShowtimeBinding
import com.example.mycinema.models.Showtime
import java.text.SimpleDateFormat
import java.util.*

class ShowtimeAdapter(
    private val onShowtimeClick: (Showtime) -> Unit
) : ListAdapter<Showtime, ShowtimeAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemShowtimeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(showtime: Showtime) = with(binding) {
            // שם הסרט
            tvMovieTitle.text = showtime.movieTitle

            // תאריך ושעה
            tvShowDate.text = formatDate(showtime.showDate)
            tvShowTime.text = showtime.showTime

            // מחיר
            if (showtime.price != null && showtime.price > 0) {
                tvPrice.text = root.context.getString(
                    R.string.price_format,
                    String.format("%.0f", showtime.price)
                )
                tvPrice.isVisible = true
            } else {
                tvPrice.isVisible = false
            }

            // זמינות
            val isAvailable = showtime.isAvailable && isShowtimeInFuture(showtime)
            tvAvailability.text = if (isAvailable) {
                root.context.getString(R.string.available)
            } else {
                root.context.getString(R.string.sold_out)
            }

            tvAvailability.setTextColor(
                if (isAvailable) {
                    root.context.getColor(R.color.success_green)
                } else {
                    root.context.getColor(R.color.error_red)
                }
            )

            // רקע לפי זמינות
            root.alpha = if (isAvailable) 1.0f else 0.6f
            root.isEnabled = isAvailable

            // לחיצה
            root.setOnClickListener {
                if (isAvailable) {
                    onShowtimeClick(showtime)
                }
            }

            // אייקון בהתאם לזמן
            val timeIcon = when {
                isShowtimeToday(showtime) -> R.drawable.ic_today
                isShowtimeTomorrow(showtime) -> R.drawable.ic_tomorrow
                else -> R.drawable.ic_calendar
            }
            ivTimeIcon.setImageResource(timeIcon)
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: dateString
            } catch (e: Exception) {
                dateString
            }
        }

        private fun isShowtimeInFuture(showtime: Showtime): Boolean {
            return try {
                val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val showtimeDateTime = dateTimeFormat.parse("${showtime.showDate} ${showtime.showTime}")
                showtimeDateTime?.after(Date()) ?: false
            } catch (e: Exception) {
                true // בספק - נניח שזה בעתיד
            }
        }

        private fun isShowtimeToday(showtime: Showtime): Boolean {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            return showtime.showDate == today
        }

        private fun isShowtimeTomorrow(showtime: Showtime): Boolean {
            val tomorrow = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
            return showtime.showDate == tomorrow
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShowtimeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Showtime>() {
        override fun areItemsTheSame(oldItem: Showtime, newItem: Showtime): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Showtime, newItem: Showtime): Boolean {
            return oldItem == newItem
        }
    }
}