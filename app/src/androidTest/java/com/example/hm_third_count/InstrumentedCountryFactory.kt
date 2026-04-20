package com.example.hm_third_count

import com.example.hm_third_count.data.model.Country
import com.example.hm_third_count.data.model.CountryFlags
import com.example.hm_third_count.data.model.CountryName

fun instrumentedCountry(
    code: String = "ITX",
    commonName: String = "InstrumentedLand"
): Country = Country(
    name = CountryName(common = commonName, official = "$commonName Official"),
    code = code,
    capital = listOf("Test Capital"),
    region = "Europe",
    population = 42L,
    flags = CountryFlags(png = "https://example.com/flag.png", svg = "https://example.com/flag.svg")
)
