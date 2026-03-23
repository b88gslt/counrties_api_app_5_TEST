package com.example.hm_third_count

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hm_third_count.data.api.CountriesApi
import com.example.hm_third_count.data.local.AppDatabase
import com.example.hm_third_count.data.model.Country
import com.example.hm_third_count.data.model.CountryFlags
import com.example.hm_third_count.data.model.CountryName
import com.example.hm_third_count.data.repository.CountriesRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Fake API для интеграционного теста
private class FakeCountriesApi(
    private val countries: List<Country> = emptyList(),
    private val shouldThrow: Boolean = false
) : CountriesApi {
    override suspend fun getAllCountries(): List<Country> {
        if (shouldThrow) throw RuntimeException("Network error")
        return countries
    }
    override suspend fun searchCountriesByName(name: String): List<Country> =
        countries.filter { it.name.common.contains(name, ignoreCase = true) }
    override suspend fun getCountryByCode(code: String): List<Country> =
        countries.filter { it.code == code }
    override suspend fun getCountriesByRegion(region: String): List<Country> =
        countries.filter { it.region == region }
}

@RunWith(AndroidJUnit4::class)
class CountriesRepositoryIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: CountriesRepository

    private val fakeCountries = listOf(
        Country(
            name = CountryName(common = "Germany", official = "Federal Republic of Germany"),
            code = "DEU",
            capital = listOf("Berlin"),
            region = "Europe",
            population = 83000000L,
            flags = CountryFlags(png = "https://flag.png", svg = "https://flag.svg")
        ),
        Country(
            name = CountryName(common = "France", official = "French Republic"),
            code = "FRA",
            capital = listOf("Paris"),
            region = "Europe",
            population = 67000000L,
            flags = CountryFlags(png = "https://flag2.png", svg = "https://flag2.svg")
        )
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // Интеграционный тест 4: Repository + Fake API + Room — полный цикл избранного
    @Test
    fun repositoryWithFakeApiAndRoom_favoriteCycle() = runTest {
        repository = CountriesRepository(FakeCountriesApi(fakeCountries), db.favoriteDao())

        // Загружаем страны
        val result = repository.getAllCountries()
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)

        // Добавляем в избранное
        repository.addToFavorites("DEU")
        assertTrue(repository.isFavorite("DEU"))
        assertFalse(repository.isFavorite("FRA"))

        // getFavorites возвращает корректный набор
        val favs = repository.getFavorites()
        assertEquals(setOf("DEU"), favs)

        // Удаляем из избранного
        repository.removeFromFavorites("DEU")
        assertFalse(repository.isFavorite("DEU"))
        assertTrue(repository.getFavorites().isEmpty())
    }

    // Интеграционный тест 5 (нетривиальный): Repository + Room — повторное добавление в избранное не создаёт дубль
    @Test
    fun repositoryDoesNotDuplicateFavorites() = runTest {
        repository = CountriesRepository(FakeCountriesApi(fakeCountries), db.favoriteDao())

        repository.addToFavorites("DEU")
        repository.addToFavorites("DEU") // дубль

        val favs = repository.getFavorites()
        assertEquals(1, favs.size)
        assertTrue(favs.contains("DEU"))
    }

    // Интеграционный тест 6: Repository с ошибкой API возвращает Result.failure
    @Test
    fun repositoryReturnsFailureOnApiError() = runTest {
        repository = CountriesRepository(FakeCountriesApi(shouldThrow = true), db.favoriteDao())

        val result = repository.getAllCountries()
        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }
}
