package world.skytale.mocks.interceptor.model

import android.content.Context
import android.util.Log
import world.skytale.mocks.interceptor.serialization.JsonConverterProvider
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

interface JsonReader {
    fun getJsonString(context: Context): String
}

data class JsonReaderFromString(val json: String) : JsonReader {
    override fun getJsonString(context: Context): String {
        return json
    }
}

open class JsonReaderFromObject<T>(val it: T, val clazz: Class<T>) : JsonReader {
    override fun getJsonString(context: Context): String {
        val moshi = JsonConverterProvider.provideMoshi()
        return moshi.adapter(clazz).toJson(it)
    }
}

open class JsonReaderFromAsset(private val fileName: String) : JsonReader {
    override fun getJsonString(context: Context): String {
        val reader = getBufferedReader(fileName, context)
        val json = reader.readText()
        reader.close()
        return json
    }

    companion object {
        fun getBufferedReader(inFileName: String, context: Context): BufferedReader {
            val assetManager = context.assets
            var inputStream: InputStream? = null
            inputStream = assetManager.open(inFileName)
            val inputStreamReader = InputStreamReader(inputStream)
            return BufferedReader(inputStreamReader)
        }

        fun assetExists(inFileName: String, context: Context): Boolean {
            return try {
                val assetManager = context.assets
                val file = assetManager.open(inFileName)
                Log.i(TAG, "assetExists: exists $inFileName")
                true
            } catch (e: Exception) {
                Log.i(TAG, "assetExists: doesntExist $inFileName")
                false
            }
        }

        private const val TAG = "JsonReader"
    }
}
