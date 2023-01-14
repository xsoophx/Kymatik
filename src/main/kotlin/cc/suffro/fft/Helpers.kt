package cc.suffro.fft

import org.kotlinmath.Complex
import org.kotlinmath.sqrt

fun abs(n: Complex): Complex = sqrt(n.re * n.re + n.im * n.im)

fun getHighestPowerOfTwo(number: Int): Int {
    var result = number
    generateSequence(1) { (it * 2) }
        .take(5)
        .forEach { position -> result = result or (result shr position) }

    return result xor (result shr 1)
}