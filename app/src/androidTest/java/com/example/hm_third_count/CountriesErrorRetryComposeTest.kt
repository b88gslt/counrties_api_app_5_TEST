package com.example.hm_third_count

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
class CountriesErrorRetryComposeTest {

    private val fakeApi = ControllableFakeCountriesApi().apply {
        failGetAllRemaining = 1
        countries = listOf(instrumentedCountry())
    }

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    @JvmField
    val countriesApi: CountriesApi = fakeApi

    @Test
    fun errorOnLoad_thenRetry_triggersNewRequestAndShowsList() {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("retry_button", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        val callsAfterError = fakeApi.getAllCallCount
        composeRule.onNodeWithTag("retry_button", useUnmergedTree = true).performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("country_row_ITX", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        assertThat(fakeApi.getAllCallCount).isGreaterThan(callsAfterError)
    }
}
