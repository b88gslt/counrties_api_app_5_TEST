package com.example.hm_third_count

import com.example.hm_third_count.data.model.ApiCountriesCurrency
import com.example.hm_third_count.data.model.ApiCountriesCountry
import com.example.hm_third_count.data.model.ApiCountriesFlags
import com.example.hm_third_count.data.model.ApiCountriesLanguage
import com.example.hm_third_count.data.model.toCountry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModelMappingTest {

    private val apiCountry = ApiCountriesCountry(
        name = "Germany",
        alpha2Code = "DE",
        alpha3Code = "DEU",
        capital = "Berlin",
        region = "Europe",
        subregion = "Western Europe",
        population = 83000000L,
        area = 357114.0,
        flags = ApiCountriesFlags(
            png = "https://flag.png",
            svg = "https://flag.svg"
        ),
        languages = listOf(ApiCountriesLanguage(name = "German", nativeName = "Deutsch")),
        currencies = listOf(ApiCountriesCurrency(code = "EUR", name = "Euro", symbol = "€")),
        timezones = listOf("UTC+01:00"),
        borders = listOf("AUT", "BEL", "CZE")
    )

    // Тест 9: корректное преобразование ApiCountriesCountry → Country
    @Test
    fun `toCountry maps all fields correctly`() {
        val country = apiCountry.toCountry()

        assertEquals("Germany", country.name.common)
        assertEquals("Germany", country.name.official)
        assertEquals("DEU", country.code)
        assertEquals(listOf("Berlin"), country.capital)
        assertEquals("Europe", country.region)
        assertEquals("Western Europe", country.subregion)
        assertEquals(83000000L, country.population)
        assertEquals(357114.0, country.area)
        assertEquals("https://flag.png", country.flags.png)
        assertEquals("https://flag.svg", country.flags.svg)
        assertEquals("German", country.languages?.values?.first())
        assertEquals("Euro", country.currencies?.get("EUR")?.name)
        assertEquals("€", country.currencies?.get("EUR")?.symbol)
        assertEquals(listOf("UTC+01:00"), country.timezones)
        assertEquals(listOf("AUT", "BEL", "CZE"), country.borders)
    }

    // Тест 10: null-поля в API-модели корректно маппятся в null в доменной модели
    @Test
    fun `toCountry handles null optional fields`() {
        val minimalApiCountry = apiCountry.copy(
            capital = null,
            subregion = null,
            area = null,
            languages = null,
            currencies = null,
            timezones = null,
            borders = null
        )

        val country = minimalApiCountry.toCountry()

        assertNull(country.capital)
        assertNull(country.subregion)
        assertNull(country.area)
        assertNull(country.languages)
        assertNull(country.currencies)
        assertNull(country.timezones)
        assertNull(country.borders)
    }
}
