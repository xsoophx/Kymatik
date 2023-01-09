package cc.suffro.fft.filters

import cc.suffro.fft.abs
import cc.suffro.fft.FFTProcessor
import kotlin.math.floor
import kotlin.math.roundToInt
import org.kotlinmath.Complex
import org.kotlinmath.complex
import org.kotlinmath.pow

// TODO: let's refactor this
class CombFilter(private val fftProcessor: FFTProcessor) {
    fun process(
        signals: Sequence<Signal>,
        bandLimits: Set<Interval>,
        maximumFrequency: Int,
        samplingRate: Int,
        timeFrame: Double
    ): Int {
        val length = signals.count()

        val fftResult = fftProcessor.process(signals, samplingRate).toList()
        val bpmInTimeFrame = (MINIMUM_BPM * timeFrame).roundToInt()
        val pulses = MutableList(length) { 0 }
        var maxEnergy = 0.0
        var estimatedBpm = 0

        for (bpm in MINIMUM_BPM..MAXIMUM_BPM step STEP_SIZE) {
            var energy = 0.0
            fillPulses(bpmInTimeFrame.toDouble(), maximumFrequency, bpm, pulses)
            val fftOfFilter = fftProcessor.process(pulses.asSequence(), samplingRate).toList()

            bandLimits.forEachIndexed { index, _ ->
                val convolution =
                    (fftResult[index].output zip fftOfFilter[index].output).map { pow(2, abs(it.first * it.second)) }
                energy += convolution.sum { it }.re
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

    private inline fun List<Complex>.sum(selector: (Complex) -> Complex): Complex {
        var result = complex(0, 0)
        for (element in this) {
            result += selector(element)
        }
        return result
    }

    private fun fillPulses(bpmInTimeFrame: Double, maximumFrequency: Int, bpm: Int, pulses: MutableList<Int>) {
        val step = floor(bpmInTimeFrame * maximumFrequency / bpm).toInt()

        for (i in 0 until PULSES) {
            pulses[i * step + 1] = 1
        }
    }

    companion object {
        const val MINIMUM_BPM = 60
        const val MAXIMUM_BPM = 220

        const val PULSES = 3
        const val STEP_SIZE = 1
    }
}