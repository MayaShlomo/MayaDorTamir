package com.example.mycinema.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mycinema.R
import com.example.mycinema.databinding.FragmentSettingsBinding
import com.example.mycinema.viewmodel.MovieViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieViewModel by activityViewModels()
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREFS_NAME = "MyCinema_Settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DEFAULT_SORT = "default_sort"
        private const val KEY_AUTO_REFRESH = "auto_refresh"
        private const val KEY_LANGUAGE_CODE = "language_code"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setupUI()
        loadSettings()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // Theme mode spinner
        val themeOptions = arrayOf(
            getString(R.string.theme_system),
            getString(R.string.theme_light),
            getString(R.string.theme_dark)
        )

        val themeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, themeOptions)
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTheme.adapter = themeAdapter

        // Sort options spinner
        val sortOptions = arrayOf(
            getString(R.string.sort_by_title),
            getString(R.string.sort_by_rating),
            getString(R.string.sort_by_year),
            getString(R.string.sort_by_date_added)
        )

        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = sortAdapter

        // Language spinner
        val languageOptions = arrayOf(
            getString(R.string.language_system),
            getString(R.string.language_english),
            getString(R.string.language_hebrew)
        )

        val languageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languageOptions)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = languageAdapter
    }

    private fun loadSettings() {
        // Load theme setting
        val themeMode = sharedPreferences.getInt(KEY_THEME_MODE, 0)
        binding.spinnerTheme.setSelection(themeMode)

        // Load sort setting
        val sortMode = sharedPreferences.getInt(KEY_DEFAULT_SORT, 0)
        binding.spinnerSort.setSelection(sortMode)

        // Load auto refresh setting
        val autoRefresh = sharedPreferences.getBoolean(KEY_AUTO_REFRESH, true)
        binding.switchAutoRefresh.isChecked = autoRefresh

        // Load language setting
        val languageCode = sharedPreferences.getString(KEY_LANGUAGE_CODE, "system") ?: "system"
        val languagePosition = when (languageCode) {
            "en" -> 1
            "he" -> 2
            else -> 0 // system
        }
        binding.spinnerLanguage.setSelection(languagePosition)
    }

    private fun setupClickListeners() {
        // Theme mode spinner
        binding.spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                saveThemeSetting(position)
                applyTheme(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Sort spinner
        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                saveSortSetting(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Language spinner - הוסרתי את הקריאה לפונקציה שלא קיימת
        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val languageCode = when (position) {
                    1 -> "en"
                    2 -> "he"
                    else -> "system"
                }
                saveLanguageSetting(languageCode)

                if (position != 0) { // Not system default
                    showLanguageChangeDialog()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Auto refresh switch
        binding.switchAutoRefresh.setOnCheckedChangeListener { _, isChecked ->
            saveAutoRefreshSetting(isChecked)
        }

        // Clear database button
        binding.btnClearDatabase.setOnClickListener {
            showClearDatabaseDialog()
        }

        // Export data button
        binding.btnExportData.setOnClickListener {
            showNotImplementedDialog(getString(R.string.export_feature))
        }

        // Import data button
        binding.btnImportData.setOnClickListener {
            showNotImplementedDialog(getString(R.string.import_feature))
        }

        // About button
        binding.btnAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.allMovies.observe(viewLifecycleOwner) { movies ->
            binding.tvTotalMovies.text = getString(R.string.total_movies_count, movies.size)
        }

        viewModel.favoriteMovies.observe(viewLifecycleOwner) { favorites ->
            binding.tvFavoriteMovies.text = getString(R.string.favorite_movies_count, favorites.size)
        }
    }

    private fun saveThemeSetting(position: Int) {
        sharedPreferences.edit().putInt(KEY_THEME_MODE, position).apply()
    }

    private fun saveSortSetting(position: Int) {
        sharedPreferences.edit().putInt(KEY_DEFAULT_SORT, position).apply()
    }

    private fun saveAutoRefreshSetting(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_AUTO_REFRESH, enabled).apply()
    }

    private fun saveLanguageSetting(languageCode: String) {
        sharedPreferences.edit().putString(KEY_LANGUAGE_CODE, languageCode).apply()
        // הוסרתי את הקריאה ל-MyCinemaApplication.updateAppLanguage
    }

    private fun applyTheme(themeMode: Int) {
        when (themeMode) {
            0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun showLanguageChangeDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.language_change_title))
            .setMessage(getString(R.string.language_change_message))
            .setPositiveButton(getString(R.string.restart_now)) { _, _ ->
                // Restart app to apply language change
                requireActivity().recreate()
            }
            .setNegativeButton(getString(R.string.restart_later), null)
            .show()
    }

    private fun showClearDatabaseDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.clear_database_title))
            .setMessage(getString(R.string.clear_database_message))
            .setPositiveButton(getString(R.string.clear)) { _, _ ->
                clearDatabase()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun clearDatabase() {
        viewModel.clearAllMovies()

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.database_cleared_title))
            .setMessage(getString(R.string.database_cleared_message))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.about_app))
            .setMessage(getString(R.string.about_message))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showNotImplementedDialog(feature: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.feature_not_implemented))
            .setMessage(getString(R.string.feature_coming_soon, feature))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}