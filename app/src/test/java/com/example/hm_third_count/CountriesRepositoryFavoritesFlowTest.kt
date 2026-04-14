package com.example.hm_third_count

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.hm_third_count.data.local.AppDatabase
import com.example.hm_third_count.data.repository.CountriesRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CountriesRepositoryFavoritesFlowTest {

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

    /**
     * Требование по Flow: полная последовательность эмиссий (не только финальное значение).
     */
    @Test
    fun favorites_emitsEmptyThenCodesThenEmpty_onInsertAndDelete() = runTest {
        val country = testCountry()
        repository.favorites.test {
            assertThat(awaitItem()).isEqualTo(emptySet<String>())
            repository.addToFavorites(country)
            assertThat(awaitItem()).containsExactly(country.code)
            repository.removeFromFavorites(country.code)
            assertThat(awaitItem()).isEqualTo(emptySet<String>())
        }
    }

    /**
     * Требование по Flow: нетривиальное потоковое поведение — корректная новая подписка:
     * каждый новый collect получает актуальное множество кодов первой эмиссией.
     */
    @Test
    fun favorites_eachNewSubscription_emitsCurrentSetFirst() = runTest {
        val c = testCountry(code = "SUB", commonName = "SubLand")
        repository.favorites.test {
            assertThat(awaitItem()).isEqualTo(emptySet<String>())
            cancelAndIgnoreRemainingEvents()
        }
        repository.addToFavorites(c)
        repository.favorites.test {
            assertThat(awaitItem()).containsExactly("SUB")
            cancelAndIgnoreRemainingEvents()
        }
        repository.favorites.test {
            assertThat(awaitItem()).containsExactly("SUB")
        }
    }

    /**
     * Нетривиальное потоковое поведение: отсутствие лишних эмиссий в буфере Turbine
     * после последней ожидаемой (без опоры только на финальный state.value).
     */
    @Test
    fun favorites_afterLastExpectedEmission_noPendingDuplicatesInChannel() = runTest {
        val c = testCountry(code = "ONE")
        repository.favorites.test {
            assertThat(awaitItem()).isEqualTo(emptySet<String>())
            repository.addToFavorites(c)
            assertThat(awaitItem()).containsExactly("ONE")
            expectNoEvents()
        }
    }

    @Test
    fun secondAddSameFavorite_stillSingleRowInRoom() {
        runBlocking {
            val country = testCountry()
            repository.addToFavorites(country)
            repository.addToFavorites(country)
            assertThat(db.favoriteDao().observeAll().first()).hasSize(1)
            assertThat(repository.favorites.first()).containsExactly(country.code)
        }
    }
}
