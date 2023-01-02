package cc.suffro.fft.data

import kotlin.math.PI
import kotlin.math.cos

fun hammingFunction(i: Int, length: Int): Double {
    val a = 25.0 / 46.0
    return a - (1 - a) * cos(2 * PI * i / length)
}

fun blackmanFunction(i: Int, length: Int): Double {
    val a0 = 0.42
    val a1 = 0.5
    val a2 = 0.08
    return a0 - a1 * cos(2 * PI * i / length) + a2 * cos(4 * PI * i / length)
}

enum class WindowFunction {
    HAMMING,
    BLACKMAN,
    NONE
}
