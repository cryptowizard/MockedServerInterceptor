package world.skytale.mocks.interceptor

import android.app.Application
import android.util.Log
import world.skytale.mocks.interceptor.GenerateMockedResponsesInterceptor.Companion.getPath
import world.skytale.mocks.interceptor.data.mockedResponseCodeMap
import world.skytale.mocks.interceptor.data.mockedResponseMap
import world.skytale.mocks.interceptor.model.JsonReaderFromAsset
import world.skytale.mocks.interceptor.util.mockedDelay
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import timber.log.Timber
import java.util.UUID

class MockedServerInterceptor(private val application: Application) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val uri = chain.request().url.toUri().toString()

        val code = getCodeForRequest(uri)
        val body = getResponseBody(uri)
        Log.i(TAG, "intercept: $uri")

        val response = Response.Builder()
            .protocol(Protocol.HTTP_2)
            .code(code)
            .body(body)
            .addHeader("content-type", "application/json")
            .request(chain.request())
            .message("")

        runBlocking {
            mockedDelay(150)
        }

        return response.build()
    }

    private fun getResponseBody(uri: String): ResponseBody {
        val responseString = getMockedResponseForRequest(uri)

        val body = if (responseString.isNotEmpty()) {
            responseString.toByteArray().toResponseBody("application/json".toMediaTypeOrNull())
        } else {
            "".toResponseBody("".toMediaTypeOrNull())
        }

        return body
    }

    private fun getMockedResponseForRequest(request: String): String {
        val address = getAddressFromRequest(request)
        Log.i("MOCK", "getMockedResponseForRequest: $address")
        return getResponseForExactRequest(address)
            ?: jsonFromResponseMap(address)
            ?: getGeneratedResponseFromAssets(request)
            ?: getJsonFromResponseMapWithoutLastParam(address)
    }

    private fun getGeneratedResponseFromAssets(request: String): String? {

        val path = getPath(request)
        val exists = JsonReaderFromAsset.assetExists(path, application)
        Log.i(TAG, "getGeneratedResponseFromAssets: $exists $path")
        return if (exists) {
            JsonReaderFromAsset(path).getJsonString(application)
        } else {
            null
        }
    }

    private fun getResponseForExactRequest(address: String): String? {
        var key = removeRequestParameters(address)
        key = removeGuiFromPath(key)

        return mockedResponseMap[key]?.getJsonString(application)
    }

    private fun getJsonFromResponseMapWithoutLastParam(address: String): String {
        val request = removeLastPathParam(address)
        val response = jsonFromResponseMap(request)
        return response ?: returnEmptyResponseForMissingMock(address)
    }

    private fun returnEmptyResponseForMissingMock(address: String): String {
        Timber.tag(TAG).e("Missing MOCK " + address)
        return ""
    }

    private fun removeLastPathParam(address: String): String {
        val index = address.lastIndexOf('/')
        return if (index > 0) {
            address.slice(0 until index)
        } else {
            ""
        }
    }

    private fun removeRequestParameters(address: String): String {
        val endIndex = if (address.indexOf('?') == -1) address.length else address.indexOf('?')
        return address.slice(0 until endIndex)
    }

    private fun jsonFromResponseMap(address: String): String? {
        val keys = mockedResponseMap.keys
        for (key in keys) {
            if (address.startsWith(key))
                return mockedResponseMap[key]?.getJsonString(application) ?: throw Exception("Missing MOCK $address")
        }
        return null
    }

    private fun removeGuiFromPath(address: String): String {
        val split = address.split("/")

        var result = ""

        split.forEach { s ->
            result += try {
                UUID.fromString(s)
                USER_ID_PATH_PARAM
            } catch (e: Exception) {
                s
            }
            result += "/"
        }

        return result.dropLast(1)
    }

    private fun getAddressFromRequest(request: String): String {
        val index = request.indexOf("?")
        val endIndex = if (index != -1) (index) else request.length

        return request.substring(0, endIndex)
    }

    private fun getCodeForRequest(request: String): Int {
        val code = mockedResponseCodeMap[request]
        return code ?: 200
    }

    companion object {
        private const val TAG = "MISSING_MOCK"
        private const val MOCKED_DELAY = 1000L
        const val USER_ID_PATH_PARAM = "userId"
        const val LONG_PATH_PARAM = "long"
    }
}
