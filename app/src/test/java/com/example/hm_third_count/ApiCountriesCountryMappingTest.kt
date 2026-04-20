package com.example.hm_third_count

import com.example.hm_third_count.data.model.ApiCountriesCountry
import com.example.hm_third_count.data.model.ApiCountriesCurrency
import com.example.hm_third_count.data.model.ApiCountriesFlags
import com.example.hm_third_count.data.model.ApiCountriesLanguage
import com.example.hm_third_count.data.model.toCountry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ApiCountriesCountryMappingTest {

    @Test
    fun toCountry_mapsAlpha3CapitalAndCurrencies() {
        val dto = ApiCountriesCountry(
            name = "United States",
            alpha2Code = "US",
            alpha3Code = "USA",
            capital = "Washington, D.C.",
            region = "Americas",
            subregion = "North America",
            population = 331_000_000L,
            area = 9_833_520.0,
            flags = ApiCountriesFlags(png = "https://png", svg = "https://svg"),
            languages = listOf(ApiCountriesLanguage(name = "English")),
            currencies = listOf(ApiCountriesCurrency(code = "USD", name = "Dollar", symbol = "$")),
            timezones = listOf("UTC-5"),
            borders = listOf("CAN", "MEX")
        )
        val c = dto.toCountry()
        assertThat(c.code).isEqualTo("USA")
        assertThat(c.name.common).isEqualTo("United States")
        assertThat(c.capital).containsExactly("Washington, D.C.")
        assertThat(c.currencies?.get("USD")?.name).isEqualTo("Dollar")
        assertThat(c.flags.png).isEqualTo("https://png")
    }
}
