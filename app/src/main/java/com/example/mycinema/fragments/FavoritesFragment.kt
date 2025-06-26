package com.example.mycinema.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mycinema.adapter.MovieAdapter
import com.example.mycinema.databinding.FragmentFavoritesBinding
import com.example.mycinema.viewmodel.MovieViewModel

class FavoritesFragment : Fragment() {
    private var _b: FragmentFavoritesBinding? = null
    private val b get() = _b!!
    private val vm: MovieViewModel by viewModels()
    private lateinit var adapter: MovieAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentFavoritesBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        Log.d("FavoritesFragment", "onViewCreated")

        // יצירת האדפטר עם listeners במקום העברת ViewModel
        adapter = MovieAdapter(
            onFavoriteClick = { movie ->
                vm.toggleFavorite(movie)
            },
            onDeleteClick = { movie ->
                vm.delete(movie)
            }
        )

        b.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FavoritesFragment.adapter
        }

        vm.favoriteMovies.observe(viewLifecycleOwner) { favorites ->
            Log.d("FavoritesFragment", "Updated favorites list. Size: ${favorites.size}")

            b.tvEmptyFavorites.isVisible = favorites.isEmpty()
            b.recyclerView.isVisible = favorites.isNotEmpty()

            if (favorites.isNotEmpty()) {
                Log.d("FavoritesFragment", "Favorites IDs: ${favorites.map { it.id }}")
                Log.d("FavoritesFragment", "First favorite: ${favorites[0].title}")
            }

            adapter.submitList(ArrayList(favorites))
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("FavoritesFragment", "onResume - forcing refresh of favorites")
        vm.refreshFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}