package world.skytale.mocks.interceptor.util

import kotlinx.coroutines.delay

val isRunningTest: Boolean by lazy {
    try {
        Class.forName("androidx.test.espresso.Espresso")
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}

suspend fun mockedDelay(time: Long) {
    if (!isRunningTest) {
        delay(time)
    }
}
