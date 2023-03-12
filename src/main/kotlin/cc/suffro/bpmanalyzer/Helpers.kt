package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.fft.data.FrequencyDomainWindow
import org.kotlinmath.Complex
import org.kotlinmath.sqrt

fun abs(n: Complex): Double = sqrt(n.re * n.re + n.im * n.im).re

fun getHighestPowerOfTwo(number: Int): Int {
    var result = number
    generateSequence(1) { (it * 2) }
        .take(5)
        .forEach { position -> result = result or (result shr position) }

    return result xor (result shr 1)
}

fun List<FrequencyDomainWindow>.interpolate(): List<FrequencyDomainWindow> {
    val interpolated = asSequence().zipWithNext().map { (current, next) ->
        FrequencyDomainWindow(
            getAverageMagnitude(current.magnitudes, next.magnitudes),
            (next.startingTime + current.startingTime) / 2
        )
    }.toList()

    return (interpolated + this).sortedBy { it.startingTime }
}

private fun getAverageMagnitude(current: List<Double>, next: List<Double>): List<Double> =
    current.zip(next).map { (c, n) -> (c + n) / 2 }

fun List<FrequencyDomainWindow>.scaleMagnitudes(): List<FrequencyDomainWindow> {
    val maximumMagnitudes = map { it.magnitudes.max() }
    val maximum = maximumMagnitudes.max()

    return map { window ->
        val scaledMagnitudes = window.magnitudes.map { it / maximum }
        FrequencyDomainWindow(scaledMagnitudes, window.startingTime)
    }
}

data class Interval<T>(
    val lowerBound: T,
    val upperBound: T
)
