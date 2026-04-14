package com.example.hm_third_count

import kotlinx.serialization.json.Json

fun testJson(): Json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}
