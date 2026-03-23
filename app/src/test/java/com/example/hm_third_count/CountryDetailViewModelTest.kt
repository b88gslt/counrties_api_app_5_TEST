package com.example.hm_third_count

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.hm_third_count.data.model.Country
import com.example.hm_third_count.data.model.CountryFlags
import com.example.hm_third_count.data.model.CountryName
import com.example.hm_third_count.data.repository.CountriesRepository
import com.example.hm_third_count.presentation.detail.CountryDetailEvent
import com.example.hm_third_count.presentation.detail.CountryDetailViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CountryDetailViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: CountriesRepository

    private val fakeCountry = Country(
        name = CountryName(common = "France", official = "French Republic"),
        code = "FRA",
        capital = listOf("Paris"),
        region = "Europe",
        population = 67000000L,
        flags = CountryFlags(png = "https://flag.png", svg = "https://flag.svg")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(code: String = "FRA"): CountryDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("countryCode" to code))
        return CountryDetailViewModel(repository, savedStateHandle)
    }

    // Тест 7: SavedStateHandle корректно передаёт countryCode и загружает нужную страну
    @Test
    fun `SavedStateHandle provides correct countryCode and loads right country`() = runTest {
        coEvery { repository.isFavorite("FRA") } returns false
        coEvery { repository.getCountryByCode("FRA") } returns Result.success(fakeCountry)

        val viewModel = createViewModel("FRA")
        advanceUntilIdle()

        assertEquals("FRA", viewModel.countryCode)
        assertEquals("France", viewModel.uiState.country?.name?.common)
        assertNull(viewModel.uiState.error)
    }

    // Тест 8 (нетривиальный): retry() на detail-экране инициирует новый запрос именно для нужного id
    @Test
    fun `retry on detail screen triggers new request for same countryCode`() = runTest {
        coEvery { repository.isFavorite("FRA") } returns false
        coEvery { repository.getCountryByCode("FRA") } returnsMany listOf(
            Result.failure(RuntimeException("Not found")),
            Result.success(fakeCountry)
        )

        val viewModel = createViewModel("FRA")
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.error)

        viewModel.onEvent(CountryDetailEvent.Retry)
        advanceUntilIdle()

        // Проверяем что запрос был именно для "FRA", дважды
        coVerify(exactly = 2) { repository.getCountryByCode("FRA") }
        assertNull(viewModel.uiState.error)
        assertEquals("France", viewModel.uiState.country?.name?.common)
    }
}
