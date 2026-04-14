package com.example.hm_third_count

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hm_third_count.data.api.CountriesApi
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class CountriesListLoadedContentTest {

    private val fakeApi = ControllableFakeCountriesApi().apply {
        countries = listOf(
            instrumentedCountry(code = "AAA", commonName = "AlphaLand"),
            instrumentedCountry(code = "BBB", commonName = "BetaLand")
        )
    }

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    @JvmField
    val countriesApi: CountriesApi = fakeApi

    @Test
    fun afterSuccessfulLoad_listShowsExpectedCountryTitles() {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("AlphaLand", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("AlphaLand", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("BetaLand", substring = true).assertIsDisplayed()
    }
}
