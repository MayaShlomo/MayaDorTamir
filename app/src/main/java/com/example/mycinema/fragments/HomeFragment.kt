package com.example.mycinema.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mycinema.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        b.btnMoviesList.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeToMoviesList())
        }
        b.btnFavorites.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeToFavorites())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}