package com.example.hm_third_count

import com.example.hm_third_count.data.model.Country
import com.example.hm_third_count.data.model.CountryFlags
import com.example.hm_third_count.data.model.CountryName

fun testCountry(
    code: String = "TST",
    commonName: String = "Testland",
    region: String = "Europe"
): Country = Country(
    name = CountryName(common = commonName, official = "$commonName Official"),
    code = code,
    capital = listOf("Test City"),
    region = region,
    population = 1_000_000L,
    flags = CountryFlags(png = "https://example.com/flag.png", svg = "https://example.com/flag.svg")
)

fun testCountrySearchMatch(code: String, name: String): Country =
    testCountry(code = code, commonName = name, region = "Asia")
