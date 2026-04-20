package com.example.hm_third_count

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.hm_third_count.data.local.AppDatabase
import com.example.hm_third_count.data.repository.CountriesRepository
import com.example.hm_third_count.presentation.countries.CountriesViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CountriesViewModelInitialStateTest {

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
    fun initialUiState_beforeCoroutinesRun_isDefault() = runTest {
        val scheduler = TestCoroutineScheduler()
        Dispatchers.setMain(StandardTestDispatcher(scheduler))
        try {
            val vm = CountriesViewModel(repository)
            val s = vm.uiState.value
            assertThat(s.countries).isEmpty()
            assertThat(s.error).isNull()
            assertThat(s.searchQuery).isEmpty()
            assertThat(s.favorites).isEmpty()
            assertThat(s.isLoading).isFalse()
        } finally {
            Dispatchers.resetMain()
        }
    }
}
