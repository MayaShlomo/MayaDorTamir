package com.example.mycinema

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.mycinema.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("MainActivity", "=== APP STARTED ===")

        // פשוט ובסיסי - ללא ActionBar מורכב
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // הוספת דיבוג לניווט
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            Log.d("MainActivity", "🧭 Navigation to: ${destination.label} (ID: ${destination.id})")
            Log.d("MainActivity", "📦 Arguments: $arguments")

            if (destination.id == R.id.movieDetailsFragment) {
                val movieId = arguments?.getInt("movieId", -1) ?: -1
                Log.d("MainActivity", "🎬 MovieDetails with ID: $movieId")

                if (movieId <= 0) {
                    Log.e("MainActivity", "❌ Invalid movieId received!")
                }
            }
        }

        Log.d("MainActivity", "✅ MainActivity setup completed")
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        Log.d("MainActivity", "🔙 Navigate up requested")
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}