package cc.suffro.bpmanalyzer.bpmanalyzing.filters

import cc.suffro.bpmanalyzer.abs
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterAnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Signal
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FFTData
import cc.suffro.bpmanalyzer.round
import mu.KotlinLogging
import kotlin.math.pow

private val logger = KotlinLogging.logger {}

class CombFilter(private val fftProcessor: FFTProcessor) {
    fun process(
        bassSignal: Signal,
        samplingRate: Int,
        params: CombFilterAnalyzerParams,
    ): Double {
        val fftResult = fftProcessor.process(bassSignal, samplingRate)
        val result = process(params.minimumBpm, params.maximumBpm, params.stepSize, bassSignal, samplingRate, fftResult)

        return if (params.refinementParams == null) {
            result
        } else {
            val newMinimumBpm = result - params.refinementParams.deviationBpm
            val newMaximumBpm = result + params.refinementParams.deviationBpm
            process(newMinimumBpm, newMaximumBpm, params.refinementParams.stepSize, bassSignal, samplingRate, fftResult)
        }
    }

    private fun process(
        minimumBpm: Double,
        maximumBpm: Double,
        stepSize: Double,
        bassSignal: Signal,
        samplingRate: Int,
        fftResult: FFTData,
    ): Double {
        var maxEnergy = 0.0
        var estimatedBpm = 0.0
        var bpm: Double = minimumBpm

        while (bpm <= maximumBpm) {
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
            bpm += stepSize
        }
        return estimatedBpm.round()
    }

    private fun fillPulses(
        bpm: Double,
        pulses: MutableList<Double>,
        samplingRate: Int,
    ) {
        val step = (1.0 / bpm * 60 * samplingRate).toInt()
        for (i in 0 until PULSES) {
            pulses[i * step] = 1.0
        }
    }

    companion object {
        const val PULSES = 3
    }
}
