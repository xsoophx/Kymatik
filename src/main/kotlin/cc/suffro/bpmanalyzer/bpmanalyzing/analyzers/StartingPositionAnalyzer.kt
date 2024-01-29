package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

import cc.suffro.bpmanalyzer.bpmanalyzing.filters.Filterbank
import cc.suffro.bpmanalyzer.wav.data.Wav

class StartingPositionAnalyzer(private val combFilterAnalyzer: CombFilterAnalyzer = CombFilterAnalyzer()) {

    fun analyze(wav: Wav): StartingPosition {
        val fullWaveRectified = Filterbank.fullWaveRectify(wav.defaultChannel())
        val bpm = combFilterAnalyzer.analyze(wav)

        val intervalSize = ((60 / bpm) * wav.sampleRate).toInt()
        // 120 bpm -> 22050 samples per interval
        val stepSize = STEP_SIZE
        val stepsPerInterval = (intervalSize / stepSize).toInt()
        val firstKick = localMaxOfIntervals(fullWaveRectified, intervalSize, stepsPerInterval)
            .groupingBy { it.first }
            .eachCount()
            .maxBy { it.value }.key

        val startAtSample = firstKick * stepsPerInterval
        return StartingPosition(startAtSample, startAtSample / wav.sampleRate.toDouble())
    }

    private fun localMaxOfIntervals(
        samples: List<Double>,
        intervalSize: Int,
        stepSizeThroughInterval: Int,
        intervals: Int = 10
    ): List<Pair<Int, Double>> {
        val maxSamples = minOf(samples.size, intervalSize * intervals)
        return samples
            .take(maxSamples)
            .chunked(intervalSize)
            .map { it.analyzeInterval(stepSizeThroughInterval) }
    }

    private fun List<Double>.analyzeInterval(stepSizeThroughInterval: Int): Pair<Int, Double> {
        return chunked(stepSizeThroughInterval).mapIndexed { index, samples ->
            val max = samples.max()
            index to max
        }.maxBy { it.second }
    }

    companion object {
        private const val STEP_SIZE = 220.5
    }
}

data class StartingPosition(val firstSample: Int, val startInSec: Double)
