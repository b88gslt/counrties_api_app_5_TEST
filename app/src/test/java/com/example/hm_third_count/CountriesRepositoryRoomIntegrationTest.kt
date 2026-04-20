package com.example.hm_third_count

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.hm_third_count.data.local.AppDatabase
import com.example.hm_third_count.data.model.Country
import com.example.hm_third_count.data.repository.CountriesRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class CountriesRepositoryRoomIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var api: FakeCountriesApi
    private lateinit var repository: CountriesRepository
    private val json = testJson()

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        api = FakeCountriesApi()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CountriesRepository(api, db.favoriteDao(), json)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun repositoryWithRoom_writeFavorite_readBackSameSnapshot() {
        runBlocking {
            val country = testCountry(code = "FIN", commonName = "Finland")
            repository.addToFavorites(country)

            val row = db.favoriteDao().observeAll().first().single()
            assertThat(row.countryCode).isEqualTo("FIN")
            val decoded: Country = json.decodeFromString<Country>(row.countrySnapshotJson!!)
            assertThat(decoded.population).isEqualTo(country.population)
            assertThat(decoded.name.common).isEqualTo("Finland")
        }
    }
}
