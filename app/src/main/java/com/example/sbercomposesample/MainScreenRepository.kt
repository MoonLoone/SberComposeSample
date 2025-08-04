package com.example.sbercomposesample

import kotlinx.coroutines.delay

class MainScreenRepository {
    suspend fun loadData(querySearch: String): Result<List<String>> {
        delay(1000)
        return Result.success(listOf("first", "second", "third"))
    }
}