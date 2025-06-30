// HomeFragment.kt - עדכון
package com.example.mycinema.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mycinema.R
import com.example.mycinema.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // אוסף הסרטים המקומי
        binding.btnMoviesList.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_moviesList)
        }

        // סרטים מועדפים
        binding.btnFavorites.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_favorites)
        }

        // חיפוש אונליין
        binding.btnSearchOnline.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_searchOnline)
        }

        // מיקומי הקרנות - NEW FEATURE!
        binding.btnCinemaLocations.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_cinemaLocations)
        }

        // הגדרות אפליקציה
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        // סטטיסטיקות
        binding.btnStatistics.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_statistics)
        }

        // הוספת סרט חדש ישירות
        binding.btnAddMovie.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_addEditMovie)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}