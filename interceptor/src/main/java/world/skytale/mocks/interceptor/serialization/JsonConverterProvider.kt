package world.skytale.mocks.interceptor.serialization

import com.squareup.moshi.Moshi

object JsonConverterProvider {
    private val moshi: Moshi by lazy {
        Moshi.Builder().add(DateTimeAdapter()).build()
    }

    fun provideMoshi(): Moshi {
        return this.moshi
    }
}
