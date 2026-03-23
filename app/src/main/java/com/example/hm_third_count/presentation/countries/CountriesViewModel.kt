package com.example.hm_third_count.presentation.countries

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hm_third_count.data.model.Country
import com.example.hm_third_count.data.repository.CountriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CountriesViewModel @Inject constructor(
    private val repository: CountriesRepository
) : ViewModel() {

    var uiState by mutableStateOf(CountriesUiState())
        private set

    // In-memory cache — loaded once, filtered locally
    private var allCountries: List<Country> = emptyList()
    private var searchJob: Job? = null

    init {
        loadCountries()
    }

    fun onEvent(event: CountriesEvent) {
        when (event) {
            is CountriesEvent.SearchQueryChanged -> {
                uiState = uiState.copy(
                    searchQuery = event.query,
                    selectedRegion = "",
                    showFavoritesOnly = false
                )
                searchCountries(event.query)
            }
            is CountriesEvent.RegionSelected -> {
                uiState = uiState.copy(
                    selectedRegion = event.region,
                    searchQuery = "",
                    showFavoritesOnly = false
                )
                filterByRegion(event.region)
            }
            CountriesEvent.ShowFavorites -> {
                uiState = uiState.copy(
                    showFavoritesOnly = true,
                    selectedRegion = "",
                    searchQuery = ""
                )
                applyFavoritesFilter()
            }
            is CountriesEvent.ToggleFavorite -> toggleFavorite(event.countryCode)
            CountriesEvent.Retry -> loadCountries()
            CountriesEvent.LoadCountries -> loadCountries()
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, searchQuery = "", selectedRegion = "", showFavoritesOnly = false)
            val favorites = repository.getFavorites()
            repository.getAllCountries()
                .onSuccess { countries ->
                    allCountries = countries
                    uiState = uiState.copy(isLoading = false, countries = countries, favorites = favorites)
                }
                .onFailure { e ->
                    uiState = uiState.copy(isLoading = false, error = e.message ?: "Unknown error", favorites = favorites)
                }
        }
    }

    private fun searchCountries(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            uiState = uiState.copy(countries = allCountries)
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            uiState = uiState.copy(isLoading = true, error = null)
            repository.searchCountries(query)
                .onSuccess { uiState = uiState.copy(isLoading = false, countries = it) }
                .onFailure { e -> uiState = uiState.copy(isLoading = false, error = e.message ?: "Search failed") }
        }
    }

    private fun filterByRegion(region: String) {
        if (region.isEmpty()) {
            uiState = uiState.copy(countries = allCountries)
            return
        }
        if (allCountries.isNotEmpty()) {
            uiState = uiState.copy(countries = allCountries.filter { c -> c.region.equals(region, ignoreCase = true) })
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            repository.getCountriesByRegion(region)
                .onSuccess { uiState = uiState.copy(isLoading = false, countries = it) }
                .onFailure { e -> uiState = uiState.copy(isLoading = false, error = e.message ?: "Filter failed") }
        }
    }

    private fun applyFavoritesFilter() {
        val favs = uiState.favorites
        uiState = uiState.copy(countries = allCountries.filter { c -> c.code in favs })
    }

    private fun toggleFavorite(countryCode: String) {
        viewModelScope.launch {
            if (repository.isFavorite(countryCode)) repository.removeFromFavorites(countryCode)
            else repository.addToFavorites(countryCode)
            val updatedFavs = repository.getFavorites()
            uiState = if (uiState.showFavoritesOnly) {
                uiState.copy(favorites = updatedFavs, countries = allCountries.filter { it.code in updatedFavs })
            } else {
                uiState.copy(favorites = updatedFavs)
            }
        }
    }
}
