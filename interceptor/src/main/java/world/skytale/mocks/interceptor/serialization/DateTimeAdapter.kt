package world.skytale.mocks.interceptor.serialization

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SERVER_DATE_FORMATTER = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
private val APP_DATE_FORMATTER = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

/**
 * Class copy from module: common
 */
class DateTimeAdapter {
    @ToJson
    fun toJson(value: Date): String {
        return SERVER_DATE_FORMATTER.format(value)
    }

    @FromJson
    fun fromJson(value: String): Date? {
        return try {
            SERVER_DATE_FORMATTER.parse(value)
        } catch (exception: Exception) {
            null
        }
    }
}

@Throws(ParseException::class)
fun String.appFormatToDate() = APP_DATE_FORMATTER.parse(this)
