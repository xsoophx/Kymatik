package cc.suffro.bpmanalyzer.bpmanalyzing.data

import cc.suffro.bpmanalyzer.Interval
import org.kotlinmath.Complex

typealias SeparatedSignals = Map<Interval<Int>, List<Complex>>
typealias Signal = Sequence<Double>
typealias Bpm = Double
