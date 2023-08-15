package world.skytale.mocks.interceptor

import android.app.Application
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 *  It's easiest to pull files using Device File Explorer
 *  then they should be copied to assets in mocked module
 */

class GenerateMockedResponsesInterceptor(private val application: Application) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        val responseBody = response.body

        responseBody?.let {
            val uri = chain.request().url.toUri().toString()
            saveToFile(getPath(uri), responseBody.getJson())
        }

        return response
    }

    private fun saveToFile(path: String, json: String) {
        val absolutePath = application.cacheDir.absolutePath

        val filename = path.split("/").last()
        val folderPath = "$absolutePath/${path.removeSuffix(filename)}"

        val folderFile = File(folderPath)
        folderFile.mkdirs()

        val file = File(folderPath, filename)
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        val output = FileOutputStream(file)
        output.write(json.toByteArray())
        output.close()
        Log.i(TAG, "saveToFile: Generated File $path")
    }

    private fun ResponseBody.getJson(): String {
        val source = this.source()
        source.request(Long.MAX_VALUE) // Buffer the entire body.
        var buffer = source.buffer
        val contentType = this.contentType()
        val charset: Charset =
            contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
        return buffer.clone().readString(charset)
    }

    companion object {
        private const val request_start = "https://api-dev-dfs.asa-international.com/"
        private const val request_start_qa = "https://api-tst-dfs.asa-international.com/"
        private const val identity_request_start = "https://identity-dev-dfs.asa-international.com/"
        private const val identity_request_start_qa = "https://identity-tst-dfs.asa-international.com/"

        fun getPath(uri: String): String {
            var address = getAddressFromRequest(uri)
            address = removePrefix(address)
            address = removeGuiFromPath(address)
            address = removeLongFromPath(address)
            return "mocks/$address.json"
        }

        private fun removePrefix(address: String): String {
            return address.removePrefix(request_start)
                .removePrefix(identity_request_start)
                .removePrefix(request_start_qa)
                .removePrefix(identity_request_start_qa)
        }

        private fun getAddressFromRequest(request: String): String {
            val index = request.indexOf("?")
            val endIndex = if (index != -1) (index) else request.length

            return request.substring(0, endIndex)
        }

        private fun removeGuiFromPath(address: String): String {
            val split = address.split("/")

            var result = ""

            split.forEach { s ->
                result += try {
                    UUID.fromString(s)
                    MockedServerInterceptor.USER_ID_PATH_PARAM
                } catch (e: Exception) {
                    s
                }
                result += "/"
            }

            return result.dropLast(1)
        }

        private fun removeLongFromPath(address: String): String {
            val split = address.split("/")

            var result = ""

            split.forEach { s ->
                result += try {
                    val long = s.toLong()
                    MockedServerInterceptor.LONG_PATH_PARAM
                } catch (e: Exception) {
                    s
                }
                result += "/"
            }

            return result.dropLast(1)
        }
    }
}

private const val TAG = "GenerateMockedResponses"
