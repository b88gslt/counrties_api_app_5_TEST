package com.example.hm_third_count

import com.example.hm_third_count.data.api.CountriesApi
import com.example.hm_third_count.data.model.Country
import java.io.IOException

class FakeCountriesApi : CountriesApi {

    var allCountries: List<Country> = emptyList()
    var getAllInvocations: Int = 0
    var failGetAll: Boolean = false

    var searchHandler: (String) -> List<Country> = { emptyList() }
    var searchInvocations: Int = 0
    var lastSearchQuery: String? = null

    var byCodeHandler: (String) -> List<Country> = { emptyList() }

    override suspend fun getAllCountries(): List<Country> {
        getAllInvocations++
        if (failGetAll) throw IOException("network error")
        return allCountries
    }

    override suspend fun searchCountriesByName(name: String): List<Country> {
        searchInvocations++
        lastSearchQuery = name
        return searchHandler(name)
    }

    override suspend fun getCountryByCode(code: String): List<Country> = byCodeHandler(code)

    override suspend fun getCountriesByRegion(region: String): List<Country> =
        allCountries.filter { it.region.equals(region, ignoreCase = true) }
}
