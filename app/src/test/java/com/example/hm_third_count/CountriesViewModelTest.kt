package com.example.hm_third_count

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.hm_third_count.data.model.Country
import com.example.hm_third_count.data.model.CountryFlags
import com.example.hm_third_count.data.model.CountryName
import com.example.hm_third_count.data.repository.CountriesRepository
import com.example.hm_third_count.presentation.countries.CountriesEvent
import com.example.hm_third_count.presentation.countries.CountriesViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CountriesViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: CountriesRepository
    private lateinit var viewModel: CountriesViewModel

    private val fakeCountry = Country(
        name = CountryName(common = "Germany", official = "Federal Republic of Germany"),
        code = "DEU",
        capital = listOf("Berlin"),
        region = "Europe",
        population = 83000000L,
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

    // Тест 1: корректное начальное состояние до завершения загрузки
    @Test
    fun `initial state has isLoading true and no error`() = runTest {
        coEvery { repository.getFavorites() } returns emptySet()
        coEvery { repository.getAllCountries() } returns Result.success(listOf(fakeCountry))

        viewModel = CountriesViewModel(repository)

        // До завершения корутины — isLoading = true
        assertTrue(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.error)
        assertTrue(viewModel.uiState.countries.isEmpty())
    }

    // Тест 2: успешная загрузка данных
    @Test
    fun `successful load populates countries and clears loading`() = runTest {
        coEvery { repository.getFavorites() } returns emptySet()
        coEvery { repository.getAllCountries() } returns Result.success(listOf(fakeCountry))

        viewModel = CountriesViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertEquals(false, state.isLoading)
        assertNull(state.error)
        assertEquals(1, state.countries.size)
        assertEquals("Germany", state.countries.first().name.common)
    }

    // Тест 3: ошибка загрузки — error != null, isLoading = false
    @Test
    fun `failed load sets error and clears loading`() = runTest {
        coEvery { repository.getFavorites() } returns emptySet()
        coEvery { repository.getAllCountries() } returns Result.failure(RuntimeException("Network error"))

        viewModel = CountriesViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertEquals(false, state.isLoading)
        assertEquals("Network error", state.error)
        assertTrue(state.countries.isEmpty())
    }

    // Тест 4 (нетривиальный): retry() действительно инициирует новый запрос к API
    @Test
    fun `retry after error triggers new API call`() = runTest {
        coEvery { repository.getFavorites() } returns emptySet()
        coEvery { repository.getAllCountries() } returnsMany listOf(
            Result.failure(RuntimeException("Network error")),
            Result.success(listOf(fakeCountry))
        )

        viewModel = CountriesViewModel(repository)
        advanceUntilIdle()

        // Убеждаемся что первый вызов дал ошибку
        assertNotNull(viewModel.uiState.error)

        // Вызываем retry
        viewModel.onEvent(CountriesEvent.Retry)
        advanceUntilIdle()

        // API должен быть вызван дважды
        coVerify(exactly = 2) { repository.getAllCountries() }

        // После retry — успешное состояние
        val state = viewModel.uiState
        assertNull(state.error)
        assertEquals(1, state.countries.size)
    }

    // Тест 5: пустой результат даёт isEmpty = true, а не Success(emptyList)
    @Test
    fun `empty result from API gives isEmpty true not success with list`() = runTest {
        coEvery { repository.getFavorites() } returns emptySet()
        coEvery { repository.getAllCountries() } returns Result.success(emptyList())

        viewModel = CountriesViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertEquals(false, state.isLoading)
        assertNull(state.error)
        assertTrue(state.countries.isEmpty())
        // isEmpty = true означает Empty-состояние, а не Success с данными
        assertTrue(state.isEmpty)
    }

    // Тест 6 (нетривиальный): состояние после ошибки и повторной загрузки переходит корректно
    @Test
    fun `state transitions correctly from error through retry to success`() = runTest {
        coEvery { repository.getFavorites() } returns emptySet()
        coEvery { repository.getAllCountries() } returnsMany listOf(
            Result.failure(RuntimeException("Timeout")),
            Result.success(listOf(fakeCountry))
        )

        viewModel = CountriesViewModel(repository)
        advanceUntilIdle()

        // Шаг 1: ошибка
        assertNotNull(viewModel.uiState.error)
        assertTrue(viewModel.uiState.countries.isEmpty())

        // Шаг 2: retry — сразу после вызова должен быть isLoading = true, error = null
        viewModel.onEvent(CountriesEvent.Retry)
        assertTrue(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.error)

        // Шаг 3: после завершения — успех
        advanceUntilIdle()
        assertNull(viewModel.uiState.error)
        assertEquals(false, viewModel.uiState.isLoading)
        assertEquals(1, viewModel.uiState.countries.size)
    }
}
