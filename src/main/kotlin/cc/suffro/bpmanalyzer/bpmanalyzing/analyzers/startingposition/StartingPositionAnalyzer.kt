package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.AnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.BpmAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.CombFilter
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.Filterbank
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import java.nio.file.Path

class StartingPositionAnalyzer(
    private val analyzer: BpmAnalyzer<CombFilter>,
    private val wavReader: FileReader<Wav>,
) : CacheAnalyzer<Wav, StartingPosition, CombFilter> {
    override fun analyze(data: Wav): StartingPosition {
        val bpm = analyzer.analyze(data)

        return analyzeByBpm(bpm, data)
    }

    private fun analyzeByBpm(
        bpm: Double,
        data: Wav,
    ): StartingPosition {
        val fullWaveRectified = Filterbank.fullWaveRectify(data.defaultChannel())

        val intervalSize = ((60 / bpm) * data.sampleRate)
        // 120 bpm -> 22050 samples per interval
        val stepSize = intervalSize / 100
        val stepsPerInterval = (intervalSize / stepSize).toInt()
        val firstKick =
            localMaxOfIntervals(fullWaveRectified, intervalSize.toInt(), stepsPerInterval)
                .groupingBy { it.first }
                .eachCount()
                .maxBy { it.value }.key

        val startAtSample = firstKick * stepsPerInterval
        return StartingPosition(startAtSample, startAtSample / data.sampleRate.toDouble())
    }

    private fun localMaxOfIntervals(
        samples: List<Double>,
        intervalSize: Int,
        stepSizeThroughInterval: Int,
        intervals: Int = 10,
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

    override fun analyze(
        data: Wav,
        params: AnalyzerParams<CombFilter>,
    ): StartingPosition {
        val startingPositionParams = params as StartingPositionCacheAnalyzerParams
        return analyzeByBpm(startingPositionParams.bpm, data)
    }

    override fun getPathAndAnalyze(path: String): StartingPosition {
        val wav = wavReader.read(path)
        return analyze(wav)
    }

    override fun getPathAndAnalyze(path: Path): StartingPosition {
        val wav = wavReader.read(path)
        return analyze(wav)
    }
}
