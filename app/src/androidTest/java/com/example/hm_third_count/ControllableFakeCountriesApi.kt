package com.example.hm_third_count

import com.example.hm_third_count.data.api.CountriesApi
import com.example.hm_third_count.data.model.Country
import java.io.IOException

class ControllableFakeCountriesApi : CountriesApi {

    var countries: List<Country> = emptyList()
    var failGetAllRemaining: Int = 0
    var getAllCallCount: Int = 0
    val getCountryByCodeArgs: MutableList<String> = mutableListOf()

    override suspend fun getAllCountries(): List<Country> {
        getAllCallCount++
        if (failGetAllRemaining > 0) {
            failGetAllRemaining--
            throw IOException("forced network failure")
        }
        return countries
    }

    override suspend fun searchCountriesByName(name: String): List<Country> =
        countries.filter { it.name.common.contains(name, ignoreCase = true) }

    override suspend fun getCountryByCode(code: String): List<Country> {
        getCountryByCodeArgs.add(code)
        return countries.filter { it.code == code }
    }

    override suspend fun getCountriesByRegion(region: String): List<Country> =
        countries.filter { it.region.equals(region, ignoreCase = true) }
}
