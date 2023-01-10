package cc.suffro.fft.bpm_analzying.data

import org.kotlinmath.Complex

typealias SeparatedSignals = Map<Interval, List<Complex>>
typealias Signal = Sequence<Double>

data class Interval(
    val lowerBound: Int,
    val upperBound: Int
)
