package world.skytale.mocks.interceptor.data

import world.skytale.mocks.interceptor.model.JsonReader

val mockedResponseMap by lazy { initializeResponsesMap() }

fun initializeResponsesMap(): MutableMap<String, JsonReader> {
    val map = mutableMapOf<String, JsonReader>()
    map.apply {
    }

    return map
}
