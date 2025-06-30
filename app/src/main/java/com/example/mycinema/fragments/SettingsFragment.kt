package com.example.mycinema.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieViewModel by activityViewModels()
    private lateinit var sharedPreferences: SharedPreferences

    private var isLoadingSettings = false

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
        // Theme options
        val themeOptions = arrayOf(
            getString(R.string.theme_system),
            getString(R.string.theme_light),
            getString(R.string.theme_dark)
        )
        val themeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, themeOptions)
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTheme.adapter = themeAdapter

        // Sort options
        val sortOptions = arrayOf(
            getString(R.string.sort_by_title),
            getString(R.string.sort_by_rating),
            getString(R.string.sort_by_year),
            getString(R.string.sort_by_date_added)
        )
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = sortAdapter

        // Language options
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
        isLoadingSettings = true

        val themeMode = sharedPreferences.getInt(KEY_THEME_MODE, 0)
        binding.spinnerTheme.setSelection(themeMode)

        val sortMode = sharedPreferences.getInt(KEY_DEFAULT_SORT, 0)
        binding.spinnerSort.setSelection(sortMode)

        val autoRefresh = sharedPreferences.getBoolean(KEY_AUTO_REFRESH, true)
        binding.switchAutoRefresh.isChecked = autoRefresh

        val languageCode = sharedPreferences.getString(KEY_LANGUAGE_CODE, "system") ?: "system"
        val languagePosition = when (languageCode) {
            "en" -> 1
            "he" -> 2
            else -> 0
        }
        binding.spinnerLanguage.setSelection(languagePosition)

        isLoadingSettings = false
    }

    private fun setupClickListeners() {
        // Theme spinner
        binding.spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isLoadingSettings) {
                    saveThemeSetting(position)
                    applyTheme(position)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Sort spinner
        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isLoadingSettings) {
                    saveSortSetting(position)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Language spinner
        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!isLoadingSettings) {
                    val currentLanguageCode = sharedPreferences.getString(KEY_LANGUAGE_CODE, "system") ?: "system"
                    val newLanguageCode = when (position) {
                        1 -> "en"
                        2 -> "he"
                        else -> "system"
                    }

                    if (currentLanguageCode != newLanguageCode) {
                        saveLanguageSetting(newLanguageCode)
                        showLanguageChangeDialog()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Auto refresh switch
        binding.switchAutoRefresh.setOnCheckedChangeListener { _, isChecked ->
            if (!isLoadingSettings) {
                saveAutoRefreshSetting(isChecked)
            }
        }

        // Buttons
        binding.btnClearDatabase.setOnClickListener { showClearDatabaseDialog() }
        binding.btnExportData.setOnClickListener { showNotImplementedDialog("Export Data") }
        binding.btnImportData.setOnClickListener { showNotImplementedDialog("Import Data") }
        binding.btnAbout.setOnClickListener { showAboutDialog() }
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
        // עדכון השפה מיד
        updateAppLanguage(languageCode)
    }

    private fun updateAppLanguage(languageCode: String) {
        val locale = when (languageCode) {
            "en" -> Locale("en", "US")
            "he" -> Locale("he", "IL")
            else -> Locale.getDefault()
        }

        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)

        // עדכון הקונטקסט
        requireContext().resources.updateConfiguration(config, requireContext().resources.displayMetrics)
        requireActivity().baseContext.resources.updateConfiguration(config, requireActivity().baseContext.resources.displayMetrics)
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
            .setPositiveButton(getString(R.string.restart_now)) { dialog, _ ->
                dialog.dismiss()
                restartApp()
            }
            .setNegativeButton(getString(R.string.restart_later)) { dialog, _ ->
                dialog.dismiss()
                Snackbar.make(binding.root, "השפה תשתנה בפתיחה הבאה", Snackbar.LENGTH_LONG).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun restartApp() {
        // פשוט ריקריאט של האקטיביטי
        requireActivity().recreate()
    }

    private fun showClearDatabaseDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.clear_database_title))
            .setMessage(getString(R.string.clear_database_message))
            .setPositiveButton(getString(R.string.clear)) { dialog, _ ->
                dialog.dismiss()
                viewModel.clearAllMovies()
                Snackbar.make(binding.root, "כל הנתונים נמחקו", Snackbar.LENGTH_LONG).show()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.about_app))
            .setMessage(getString(R.string.about_message))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showNotImplementedDialog(feature: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("לא זמין")
            .setMessage("$feature יתווסף בגרסה הבאה")
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}