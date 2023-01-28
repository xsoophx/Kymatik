package cc.suffro.fft.bpmanalyzing.filters

import cc.suffro.fft.abs
import cc.suffro.fft.bpmanalyzing.data.Signal
import cc.suffro.fft.fft.FFTProcessor
import mu.KotlinLogging
import kotlin.math.pow

private val logger = KotlinLogging.logger {}

// TODO: let's refactor this
class CombFilter(private val fftProcessor: FFTProcessor) {
    fun process(bassSignal: Signal, samplingRate: Int): Int {
        val fftResult = fftProcessor.process(bassSignal, samplingRate)
        var maxEnergy = 0.0
        var estimatedBpm = 0

        for (bpm in MINIMUM_BPM..MAXIMUM_BPM step STEP_SIZE) {
            var energy = 0.0
            val pulses = MutableList(bassSignal.count()) { 0.0 }
            fillPulses(bpm, pulses, samplingRate)
            val fftOfFilter = fftProcessor.process(pulses.asSequence(), samplingRate)

            val convolution = (fftResult.output zip fftOfFilter.output).map { abs(it.first * it.second).pow(2) }
            energy += convolution.sum()

            logger.info { "$bpm BPM with energy: $energy." }
            if (energy > maxEnergy) {
                estimatedBpm = bpm
                maxEnergy = energy
            }
        }
        return estimatedBpm
    }

    private fun fillPulses(bpm: Int, pulses: MutableList<Double>, samplingRate: Int) {
        val step = (1.0 / bpm * 60 * samplingRate).toInt()
        for (i in 0 until PULSES) {
            pulses[i * step] = 1.0
        }
    }

    companion object {
        const val MINIMUM_BPM = 60
        const val MAXIMUM_BPM = 220

        const val PULSES = 3
        const val STEP_SIZE = 1
    }
}
