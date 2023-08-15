package world.skytale.mocks.interceptor.data

import world.skytale.mocks.interceptor.model.JsonReader
import world.skytale.mocks.interceptor.model.JsonReaderFromString

/**
 * This map overrides mocked response map, (it is checked before, so if the request matches one here it will be used)
 */
object MockedErrorResponses {
    fun setResponseError(url: String, code: Int = 500, response: JsonReader = JsonReaderFromString("")) {
        mockedResponseCodeMap[url] = code
        mockedResponseMap[url] = response
    }
}
