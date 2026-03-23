package com.example.hm_third_count

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hm_third_count.data.model.Country
import com.example.hm_third_count.data.model.CountryFlags
import com.example.hm_third_count.data.model.CountryName
import com.example.hm_third_count.presentation.countries.CountriesEvent
import com.example.hm_third_count.presentation.countries.CountriesScreen
import com.example.hm_third_count.presentation.countries.CountriesUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CountriesScreenIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fakeCountry = Country(
        name = CountryName(common = "Germany", official = "Federal Republic of Germany"),
        code = "DEU",
        capital = listOf("Berlin"),
        region = "Europe",
        population = 83000000L,
        flags = CountryFlags(png = "https://flag.png", svg = "https://flag.svg")
    )

    // UI-интеграционный тест 7: отображение корректного состояния экрана после успешной загрузки
    @Test
    fun successState_displaysCountryList() {
        val state = CountriesUiState(
            isLoading = false,
            countries = listOf(fakeCountry),
            error = null
        )

        composeRule.setContent {
            CountriesScreen(
                uiState = state,
                onEvent = {},
                onCountryClick = {}
            )
        }

        composeRule.onNodeWithText("Germany").assertIsDisplayed()
        composeRule.onNodeWithText("Berlin").assertIsDisplayed()
    }

    // UI-интеграционный тест 8: ошибка → нажатие Retry → успешное состояние
    @Test
    fun errorState_retryClick_triggersRetryEvent() {
        var retryClicked = false
        val errorState = CountriesUiState(
            isLoading = false,
            countries = emptyList(),
            error = "Network error"
        )

        composeRule.setContent {
            CountriesScreen(
                uiState = errorState,
                onEvent = { event ->
                    if (event is CountriesEvent.Retry) retryClicked = true
                },
                onCountryClick = {}
            )
        }

        composeRule.onNodeWithText("Network error").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()

        assert(retryClicked) { "Retry event was not fired" }
    }

    // UI-интеграционный тест 9 (нетривиальный): клик по элементу списка вызывает навигацию с правильным id
    @Test
    fun countryItemClick_navigatesToCorrectCountryCode() {
        var navigatedCode: String? = null
        val state = CountriesUiState(
            isLoading = false,
            countries = listOf(fakeCountry),
            error = null
        )

        composeRule.setContent {
            CountriesScreen(
                uiState = state,
                onEvent = {},
                onCountryClick = { code -> navigatedCode = code }
            )
        }

        composeRule.onNodeWithText("Germany").performClick()

        assert(navigatedCode == "DEU") {
            "Expected navigation to DEU but got $navigatedCode"
        }
    }
}
