package com.example.hm_third_count

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.hm_third_count.data.local.AppDatabase
import com.example.hm_third_count.data.repository.CountriesRepository
import com.example.hm_third_count.presentation.countries.CountriesEvent
import com.example.hm_third_count.presentation.countries.CountriesViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CountriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var db: AppDatabase
    private lateinit var api: FakeCountriesApi
    private lateinit var repository: CountriesRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        api = FakeCountriesApi()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CountriesRepository(api, db.favoriteDao(), testJson())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun loadCountries_success_updatesList() {
        val country = testCountry()
        api.allCountries = listOf(country)
        val vm = CountriesViewModel(repository)
        val state = vm.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
        assertThat(state.countries).containsExactly(country)
    }

    @Test
    fun loadCountries_failure_thenRetry_succeeds() {
        api.failGetAll = true
        val vm = CountriesViewModel(repository)
        assertThat(vm.uiState.value.error).isNotNull()
        assertThat(api.getAllInvocations).isEqualTo(1)

        api.failGetAll = false
        api.allCountries = listOf(testCountry())
        vm.onEvent(CountriesEvent.Retry)

        assertThat(vm.uiState.value.error).isNull()
        assertThat(vm.uiState.value.countries).hasSize(1)
        assertThat(api.getAllInvocations).isEqualTo(2)
    }

    @Test
    fun searchEmptyResult_showsEmptyState_notSuccessWithData() = runTest {
        val scheduler = TestCoroutineScheduler()
        val main = StandardTestDispatcher(scheduler)
        Dispatchers.setMain(main)
        try {
            api.allCountries = listOf(testCountry())
            val vm = CountriesViewModel(repository)
            scheduler.advanceUntilIdle()
            assertThat(vm.uiState.value.countries).isNotEmpty()

            api.searchHandler = { emptyList() }
            vm.onEvent(CountriesEvent.SearchQueryChanged("zzz"))
            scheduler.advanceTimeBy(350)
            scheduler.advanceUntilIdle()

            val s = vm.uiState.value
            assertThat(s.isLoading).isFalse()
            assertThat(s.error).isNull()
            assertThat(s.countries).isEmpty()
            assertThat(s.isEmpty).isTrue()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun search_onlyLatestQueryRuns_afterRapidTyping() = runTest {
        val scheduler = TestCoroutineScheduler()
        val main = StandardTestDispatcher(scheduler)
        Dispatchers.setMain(main)
        try {
            api.allCountries = listOf(testCountry(code = "OLD", commonName = "Old"))
            val vm = CountriesViewModel(repository)
            scheduler.advanceUntilIdle()

            api.searchHandler = { q ->
                listOf(testCountrySearchMatch(code = "NEW", name = "Result-$q"))
            }

            vm.onEvent(CountriesEvent.SearchQueryChanged("a"))
            scheduler.advanceTimeBy(100)
            vm.onEvent(CountriesEvent.SearchQueryChanged("b"))
            scheduler.advanceTimeBy(350)
            scheduler.advanceUntilIdle()

            assertThat(api.searchInvocations).isEqualTo(1)
            assertThat(api.lastSearchQuery).isEqualTo("b")
            assertThat(vm.uiState.value.countries.single().code).isEqualTo("NEW")
            assertThat(vm.uiState.value.countries.single().name.common).isEqualTo("Result-b")
        } finally {
            Dispatchers.resetMain()
        }
    }
}
