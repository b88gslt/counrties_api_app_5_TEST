package com.example.hm_third_count.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hm_third_count.presentation.countries.CountriesScreen
import com.example.hm_third_count.presentation.countries.CountriesViewModel
import com.example.hm_third_count.presentation.detail.CountryDetailScreen
import com.example.hm_third_count.presentation.detail.CountryDetailViewModel

@Composable
fun CountriesNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "countries") {
        composable("countries") {
            val viewModel: CountriesViewModel = hiltViewModel()
            CountriesScreen(
                uiState = viewModel.uiState,
                onEvent = viewModel::onEvent,
                onCountryClick = { navController.navigate("detail/$it") }
            )
        }

        composable("detail/{countryCode}") {
            val viewModel: CountryDetailViewModel = hiltViewModel()
            CountryDetailScreen(
                uiState = viewModel.uiState,
                onEvent = viewModel::onEvent,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
