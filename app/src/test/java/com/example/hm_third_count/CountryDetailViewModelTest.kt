package com.example.hm_third_count

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.hm_third_count.data.local.AppDatabase
import com.example.hm_third_count.data.repository.CountriesRepository
import com.example.hm_third_count.presentation.detail.CountryDetailEvent
import com.example.hm_third_count.presentation.detail.CountryDetailViewModel
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CountryDetailViewModelTest {

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
    fun usesCountryCodeFromSavedStateHandle() {
        val country = testCountry(code = "AAA", commonName = "Alpha")
        api.byCodeHandler = { code ->
            assertThat(code).isEqualTo("AAA")
            listOf(country)
        }
        val handle = SavedStateHandle(mapOf("countryCode" to "AAA"))
        val vm = CountryDetailViewModel(repository, handle)
        val s = vm.uiState.value
        assertThat(s.country?.code).isEqualTo("AAA")
        assertThat(s.country?.name?.common).isEqualTo("Alpha")
        assertThat(s.error).isNull()
    }

    @Test
    fun retry_afterFailure_callsApiAgain() {
        var calls = 0
        api.byCodeHandler = {
            calls++
            if (calls == 1) throw java.io.IOException("fail")
            listOf(testCountry(code = "Z", commonName = "Zed"))
        }
        val vm = CountryDetailViewModel(repository, SavedStateHandle(mapOf("countryCode" to "Z")))
        assertThat(vm.uiState.value.error).isNotNull()
        assertThat(calls).isEqualTo(1)

        vm.onEvent(CountryDetailEvent.Retry)
        assertThat(calls).isEqualTo(2)
        assertThat(vm.uiState.value.country?.name?.common).isEqualTo("Zed")
        assertThat(vm.uiState.value.error).isNull()
    }
}
