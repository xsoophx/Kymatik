package cc.suffro.fft.bpmanalyzing.filters

import cc.suffro.fft.abs
import cc.suffro.fft.bpmanalyzing.data.Interval
import cc.suffro.fft.bpmanalyzing.data.Signal
import cc.suffro.fft.fft.FFTProcessor
import kotlin.math.pow

// TODO: let's refactor this
class CombFilter(private val fftProcessor: FFTProcessor) {
    fun process(
        signals: Sequence<Signal>,
        bandLimits: Set<Interval>,
        maximumFrequency: Int,
        samplingRate: Int
    ): Int {
        val first = signals.first()
        val length = first.count()

        val fftResult = fftProcessor.process(signals, samplingRate).toList()
        // val bpmInTimeFrame = (MINIMUM_BPM * timeFrame).roundToInt()
        var maxEnergy = 0.0
        var estimatedBpm = 0

        for (bpm in MINIMUM_BPM..MAXIMUM_BPM step STEP_SIZE) {
            var energy = 0.0
            val pulses = MutableList(length) { 0 }
            fillPulses(120.0, maximumFrequency, bpm, pulses)
            val fftOfFilter = fftProcessor.process(pulses.asSequence(), samplingRate).toList().first()

            bandLimits.forEachIndexed { index, _ ->
                val convolution =
                    (fftResult[index].output zip fftOfFilter.output).map { abs(it.first * it.second).pow(2) }
                energy += convolution.sum()
            }

            if (energy > maxEnergy) {
                estimatedBpm = bpm
                maxEnergy = energy
            }
        }
        return estimatedBpm
    }

    private fun MutableList<Int>.asSequence(): Sequence<Sequence<Double>> {
        return sequenceOf(map { it.toDouble() }.asSequence())
    }

    private fun fillPulses(bpmInTimeFrame: Double, maximumFrequency: Int, bpm: Int, pulses: MutableList<Int>) {
        val step = (bpmInTimeFrame / bpm * maximumFrequency).toInt() - 1

        for (i in 0 until PULSES) {
            pulses[i * step] = 1
        }
    }

    companion object {
        const val MINIMUM_BPM = 60
        const val MAXIMUM_BPM = 220

        const val PULSES = 3
        const val STEP_SIZE = 1
    }
}
