package cc.suffro.bpmanalyzer.bpmanalyzing.data

import org.kotlinmath.Complex

typealias SeparatedSignals = Map<Interval, List<Complex>>
typealias Signal = Sequence<Double>
typealias Bpm = Double

data class Interval(
    val lowerBound: Int,
    val upperBound: Int
)
