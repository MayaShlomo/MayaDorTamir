// CinemaLocationsFragment.kt
// החלף את: app/src/main/java/com/example/mycinema/fragments/CinemaLocationsFragment.kt
package com.example.mycinema.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mycinema.R
import com.example.mycinema.databinding.FragmentCinemaLocationsBinding
import com.example.mycinema.models.Cinema
import com.example.mycinema.models.CinemaWithDistance
import com.example.mycinema.viewmodel.CinemaViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CinemaLocationsFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentCinemaLocationsBinding? = null
    private val binding get() = _binding!!

    private val cinemaViewModel: CinemaViewModel by activityViewModels()
    private lateinit var googleMap: GoogleMap
    private var isMapReady = false

    // הרשאות מיקום
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                cinemaViewModel.checkLocationPermission()
                enableMyLocation()
                cinemaViewModel.updateUserLocation()
            }
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                cinemaViewModel.checkLocationPermission()
                enableMyLocation()
                cinemaViewModel.updateUserLocation()
            }
            else -> {
                showLocationPermissionDeniedDialog()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCinemaLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("CinemaLocationsFragment", "Fragment created")

        setupClickListeners()
        setupDataSourceMenu()
        observeViewModel()
        checkLocationPermission()

        // אתחול המפה
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d("CinemaLocationsFragment", "Map is ready")
        googleMap = map
        isMapReady = true

        setupMap()

        // *** טעינת בתי קולנוע מפורסמים כברירת מחדל ***
        Log.d("CinemaLocationsFragment", "Map ready, loading famous cinemas worldwide")
        cinemaViewModel.loadAllFamousCinemas()
    }

    private fun setupMap() {
        googleMap.apply {
            // הגדרות UI
            uiSettings.apply {
                isZoomControlsEnabled = true
                isMyLocationButtonEnabled = true
                isCompassEnabled = true
                isMapToolbarEnabled = true
            }

            // סגנון המפה (אופציונלי)
            try {
                setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_style_dark
                    )
                )
            } catch (e: Exception) {
                Log.w("CinemaLocationsFragment", "Can't load map style: ${e.message}")
            }

            // לחיצה על מרקר
            setOnMarkerClickListener { marker ->
                val cinema = marker.tag as? Cinema
                cinema?.let {
                    showCinemaInfoWindow(it, marker)
                }
                true
            }

            // לחיצה על info window
            setOnInfoWindowClickListener { marker ->
                val cinema = marker.tag as? Cinema
                cinema?.let {
                    showCinemaDetailsDialog(it)
                }
            }

            // מיקום ברירת מחדל (זום על כל העולם)
            val worldCenter = LatLng(20.0, 0.0)
            moveCamera(CameraUpdateFactory.newLatLngZoom(worldCenter, 2f))
        }

        // אפשור מיקום אם יש הרשאה
        if (hasLocationPermission()) {
            enableMyLocation()
        }
    }

    private fun setupDataSourceMenu() {
        binding.btnDataSource?.setOnClickListener {
            // אם אין נתונים כלל, טען הכל
            if (cinemaViewModel.nearbyCinemas.value.isNullOrEmpty()) {
                cinemaViewModel.loadAllFamousCinemas()
            } else {
                showDataSourceDialog()
            }
        }

        // הגדרת הטקסט הראשוני
        updateDataSourceButton("🌟 Famous Cinemas")
    }

    private fun showDataSourceDialog() {
        val options = arrayOf(
            "🌟 Famous Cinemas Worldwide",
            "🎬 Sample Data (with Showtimes)",
            "🌍 All Famous Cinemas",
            "🇮🇱 Israeli Cinemas",
            "🇺🇸 US Cinemas",
            "🇬🇧 UK Cinemas",
            "🇫🇷 French Cinemas",
            "🇩🇪 German Cinemas",
            "🇯🇵 Japanese Cinemas",
            "🔍 Search Cinemas"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Cinema Data Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        updateDataSourceButton("🌟 Famous")
                        cinemaViewModel.loadFamousCinemas(200.0)
                    }
                    1 -> {
                        updateDataSourceButton("🎬 Sample")
                        cinemaViewModel.loadNearbyCinemas()
                    }
                    2 -> {
                        updateDataSourceButton("🌍 All Famous")
                        cinemaViewModel.loadAllFamousCinemas()
                    }
                    3 -> {
                        updateDataSourceButton("🇮🇱 Israeli")
                        cinemaViewModel.loadCinemasByCountry("israel")
                    }
                    4 -> {
                        updateDataSourceButton("🇺🇸 US")
                        cinemaViewModel.loadCinemasByCountry("usa")
                    }
                    5 -> {
                        updateDataSourceButton("🇬🇧 UK")
                        cinemaViewModel.loadCinemasByCountry("uk")
                    }
                    6 -> {
                        updateDataSourceButton("🇫🇷 French")
                        cinemaViewModel.loadCinemasByCountry("france")
                    }
                    7 -> {
                        updateDataSourceButton("🇩🇪 German")
                        cinemaViewModel.loadCinemasByCountry("germany")
                    }
                    8 -> {
                        updateDataSourceButton("🇯🇵 Japanese")
                        cinemaViewModel.loadCinemasByCountry("japan")
                    }
                    9 -> {
                        showSearchDialog()
                    }
                }
            }
            .show()
    }

    private fun showSearchDialog() {
        val editText = EditText(requireContext())
        editText.hint = "Enter cinema name or city..."

        AlertDialog.Builder(requireContext())
            .setTitle("Search Cinemas")
            .setView(editText)
            .setPositiveButton("Search") { _, _ ->
                val query = editText.text.toString().trim()
                if (query.isNotEmpty()) {
                    updateDataSourceButton("🔍 $query")
                    cinemaViewModel.searchCinemas(query)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateDataSourceButton(text: String) {
        binding.btnDataSource?.text = text
    }

    private fun setupClickListeners() {
        binding.btnListView.setOnClickListener {
            findNavController().navigate(R.id.action_cinemaLocations_to_list)
        }

        binding.btnRefreshLocation.setOnClickListener {
            refreshLocation()
        }

        binding.fabMyLocation.setOnClickListener {
            centerOnUserLocation()
        }
    }

    private fun observeViewModel() {
        // בתי קולנוע בקרבת מקום
        cinemaViewModel.nearbyCinemas.observe(viewLifecycleOwner) { cinemas ->
            Log.d("CinemaLocationsFragment", "Received ${cinemas.size} cinemas")
            if (isMapReady) {
                addCinemaMarkers(cinemas)

                // התמקדות בבתי הקולנוע אם יש
                if (cinemas.isNotEmpty()) {
                    focusOnCinemas(cinemas)
                }
            }
        }

        // מיקום משתמש
        cinemaViewModel.userLocation.observe(viewLifecycleOwner) { userLocation ->
            userLocation?.let {
                Log.d("CinemaLocationsFragment", "User location updated: ${it.latitude}, ${it.longitude}")
                if (isMapReady) {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(userLatLng, 12f)
                    )
                }
            }
        }

        // מצב טעינה
        cinemaViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // שגיאות
        cinemaViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                showError(errorMessage)
                cinemaViewModel.clearError()
            }
        }

        // הרשאות מיקום
        cinemaViewModel.hasLocationPermission.observe(viewLifecycleOwner) { hasPermission ->
            binding.btnRefreshLocation.isEnabled = hasPermission
            binding.fabMyLocation.isEnabled = hasPermission
        }

        // סוג מקור הנתונים
        cinemaViewModel.dataSourceType.observe(viewLifecycleOwner) { dataSourceType ->
            updateDataSourceIndicator(dataSourceType)
        }
    }

    private fun updateDataSourceIndicator(dataSourceType: CinemaViewModel.DataSourceType) {
        val indicator = when (dataSourceType) {
            CinemaViewModel.DataSourceType.FAMOUS_CINEMAS -> "🌟"
            CinemaViewModel.DataSourceType.SAMPLE_DATA -> "🎬"
            CinemaViewModel.DataSourceType.COMBINED -> "🌍"
            CinemaViewModel.DataSourceType.BY_COUNTRY -> "🏛️"
        }
        binding.tvDataSourceIndicator?.text = indicator
    }

    private fun focusOnCinemas(cinemas: List<CinemaWithDistance>) {
        if (cinemas.isEmpty()) return

        try {
            val bounds = LatLngBounds.Builder()
            cinemas.forEach { cinemaWithDistance ->
                bounds.include(LatLng(cinemaWithDistance.cinema.latitude, cinemaWithDistance.cinema.longitude))
            }

            val padding = 100
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds.build(), padding)
            )
            Log.d("CinemaLocationsFragment", "Camera focused on ${cinemas.size} cinemas")
        } catch (e: Exception) {
            Log.e("CinemaLocationsFragment", "Error focusing on cinemas", e)
            // fallback - התמקד על הראשון
            val firstCinema = cinemas.first().cinema
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(firstCinema.latitude, firstCinema.longitude),
                    5f // זום בינוני
                )
            )
        }
    }

    private fun addCinemaMarkers(cinemas: List<CinemaWithDistance>) {
        if (!isMapReady) return

        Log.d("CinemaLocationsFragment", "Adding ${cinemas.size} cinema markers")
        googleMap.clear()

        cinemas.forEach { cinemaWithDistance ->
            val cinema = cinemaWithDistance.cinema
            val position = LatLng(cinema.latitude, cinema.longitude)

            val markerOptions = MarkerOptions()
                .position(position)
                .title(cinema.name)
                .snippet(createMarkerSnippet(cinema, cinemaWithDistance.distance))
                .icon(createCinemaMarkerIcon(cinema))

            val marker = googleMap.addMarker(markerOptions)
            marker?.tag = cinema
        }
    }

    private fun createMarkerSnippet(cinema: Cinema, distance: Double): String {
        return buildString {
            if (distance > 0) {
                append("${String.format("%.1f", distance)} km away")
            }
            if (cinema.rating > 0) {
                append(" • ${String.format("%.1f", cinema.rating)}★")
            }
            if (!cinema.phone.isNullOrBlank()) {
                append(" • Call available")
            }
        }
    }

    private fun createCinemaMarkerIcon(cinema: Cinema): BitmapDescriptor {
        return when {
            cinema.rating >= 4.5f -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            cinema.rating >= 4.0f -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
            cinema.rating >= 3.5f -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        }
    }

    private fun showCinemaInfoWindow(cinema: Cinema, marker: Marker) {
        marker.showInfoWindow()
    }

    private fun showCinemaDetailsDialog(cinema: Cinema) {
        val message = buildString {
            append(cinema.address)
            append("\n\n")

            if (cinema.rating > 0) {
                append("Rating: ${String.format("%.1f", cinema.rating)}★\n")
            }

            if (!cinema.phone.isNullOrBlank()) {
                append("Phone: ${cinema.phone}\n")
            }

            if (!cinema.website.isNullOrBlank()) {
                append("Website: Available\n")
            }

            append("\n🌟 Famous cinema from around the world")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(cinema.name)
            .setMessage(message)
            .setPositiveButton("Navigate") { _, _ ->
                openNavigation(cinema)
            }
            .setNeutralButton("Call") { _, _ ->
                cinema.phone?.let { phone ->
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phone")
                    }
                    startActivity(intent)
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun openNavigation(cinema: Cinema) {
        val uri = Uri.parse("geo:${cinema.latitude},${cinema.longitude}?q=${cinema.latitude},${cinema.longitude}(${cinema.name})")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            val webUri = Uri.parse("https://maps.google.com/?q=${cinema.latitude},${cinema.longitude}")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            startActivity(webIntent)
        }
    }

    private fun checkLocationPermission() {
        when {
            hasLocationPermission() -> {
                cinemaViewModel.checkLocationPermission()
                if (isMapReady) {
                    enableMyLocation()
                }
                cinemaViewModel.updateUserLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showPermissionRationaleDialog()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isMapReady && hasLocationPermission()) {
            googleMap.isMyLocationEnabled = true
        }
    }

    private fun refreshLocation() {
        if (hasLocationPermission()) {
            cinemaViewModel.updateUserLocation()
            cinemaViewModel.refresh()
        } else {
            requestLocationPermission()
        }
    }

    private fun centerOnUserLocation() {
        cinemaViewModel.userLocation.value?.let { location ->
            if (isMapReady) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(userLatLng, 14f)
                )
            }
        } ?: run {
            refreshLocation()
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission")
            .setMessage("This app needs location permission to show nearby cinemas on the map.")
            .setPositiveButton("Grant Permission") { _, _ ->
                requestLocationPermission()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLocationPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Denied")
            .setMessage("Location permission is required to show your position and nearby cinemas. You can still browse all cinemas worldwide.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    // Map lifecycle methods
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}