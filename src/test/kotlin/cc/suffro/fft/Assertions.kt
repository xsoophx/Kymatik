package cc.suffro.fft

import kotlin.math.abs
import kotlin.test.assertTrue
import org.kotlinmath.Complex

fun assertNearlyEquals(expected: Double, actual: Double, e: Double = 0.2, message: String? = null) {
    assertTrue(actual.closeTo(expected, e), message ?: "Expected <$expected>, actual <$actual>.")
}

fun assertNearlyEquals(expected: Complex, actual: Complex, e: Double = 2.0, message: String? = null) {
    assertTrue(actual.closeTo(expected, e), message ?: "Expected <$expected>, actual <$actual>.")
}

private fun Complex.closeTo(number: Complex, e: Double): Boolean =
    abs(re - number.re) < e && abs(im - number.im) < e

private fun Double.closeTo(number: Double, e: Double): Boolean =
    abs(this - number) < e
