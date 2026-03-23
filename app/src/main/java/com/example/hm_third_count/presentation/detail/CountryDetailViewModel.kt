package com.example.hm_third_count.presentation.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hm_third_count.data.repository.CountriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CountryDetailViewModel @Inject constructor(
    private val repository: CountriesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val countryCode: String = checkNotNull(savedStateHandle["countryCode"])

    var uiState by mutableStateOf(CountryDetailUiState())
        private set

    init {
        loadCountryDetail()
    }

    fun onEvent(event: CountryDetailEvent) {
        when (event) {
            CountryDetailEvent.Retry -> loadCountryDetail()
            CountryDetailEvent.ToggleFavorite -> toggleFavorite()
        }
    }

    private fun loadCountryDetail() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val isFav = repository.isFavorite(countryCode)
            repository.getCountryByCode(countryCode)
                .onSuccess { uiState = uiState.copy(isLoading = false, country = it, isFavorite = isFav) }
                .onFailure { e -> uiState = uiState.copy(isLoading = false, error = e.message ?: "Failed to load country details", isFavorite = isFav) }
        }
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            if (repository.isFavorite(countryCode)) repository.removeFromFavorites(countryCode)
            else repository.addToFavorites(countryCode)
            uiState = uiState.copy(isFavorite = repository.isFavorite(countryCode))
        }
    }
}
