package com.example.hm_third_count

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hm_third_count.data.api.CountriesApi
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class CountriesListDetailNavigationTest {

    private val fakeApi = ControllableFakeCountriesApi().apply {
        countries = listOf(instrumentedCountry(code = "ITX", commonName = "InstrumentedLand"))
    }

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    @JvmField
    val countriesApi: CountriesApi = fakeApi

    @Test
    fun list_clickNavigatesToDetail_forMatchingCountryCode() {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("country_row_ITX", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("country_row_ITX", useUnmergedTree = true).performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("detail_basic_information", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("detail_top_bar_title", useUnmergedTree = true)
            .assertTextContains("InstrumentedLand", ignoreCase = true)
        assertThat(fakeApi.getCountryByCodeArgs).contains("ITX")
    }
}
