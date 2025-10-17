package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.AnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.Analyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.CombFilterOperations
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.LowPassFilter
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.TimeDomainWindow
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import kotlin.math.exp
import kotlin.math.ln

class StartingPositionAnalyzer(
    private val analyzer: Analyzer<Wav, TrackInfo>,
    private val wavReader: FileReader<Wav>,
    private val combFilterOperations: CombFilterOperations,
    private val fftProcessor: FFTProcessor,
) : CacheAnalyzer<Wav, StartingPosition> {
    override fun analyze(data: Wav): StartingPosition {
        val trackInfo = analyzer.analyze(data)
        return analyze(data, trackInfo.bpm)
    }

    override fun getPathAndAnalyze(path: String): StartingPosition {
        return getPathAndAnalyze(Path.of(path))
    }

    override fun getPathAndAnalyze(path: Path): StartingPosition {
        val wav = wavReader.read(path)
        val trackInfo = analyzer.analyze(wav)
        return analyze(wav, trackInfo.bpm)
    }

    override fun analyze(
        data: Wav,
        params: AnalyzerParams,
    ): StartingPosition {
        val bpm = (params as StartingPositionCacheAnalyzerParams).bpm
        return analyze(data, bpm)
    }

    // TODO: too much guessing, too unprecise but okay for now
    //  use comb filter with determined bpm and move it over the start of the track
    private fun analyze(
        data: Wav,
        bpm: Bpm,
        samplesToSkip: Int = 0,
    ): StartingPosition {
        logger.info { "Analyzing starting position of track: ${data.filePath} with bpm: $bpm" }

        val sampleSizeToAnalyze = (ANALYZING_DURATION * data.sampleRate).toInt()

        // TODO: this could be streamed to avoid loading all samples in memory
        val samples = data.defaultChannel().drop(samplesToSkip)

        val k = ln(1000.0) / (FFT_SAMPLES - 1) // f(N-1) ca. 1/1000
        val eFunction = (0 until FFT_SAMPLES).map { i -> exp(-k * i) }

        val fftResults =
            samples
                .take(sampleSizeToAnalyze)
                .windowed(FFT_SAMPLES, STEP_SIZE, partialWindows = false)
                .mapIndexed { idx, window ->
                    val startSample = idx * STEP_SIZE
                    val startTime = startSample / data.sampleRate.toDouble()
                    val duration = window.size / data.sampleRate.toDouble()
                    val tdWindow = TimeDomainWindow(window.asSequence(), duration, startTime)

                    val lowPassed = LowPassFilter.processFrequencyDomainFFTData(tdWindow, data.sampleRate)

                    // add weighting function to magnitudes
                    val weightedMagnitudes = lowPassed.magnitudes.zip(eFunction).map { (mag, factor) -> mag * factor }
                    val maxValue = weightedMagnitudes.maxOrNull() ?: 0.0
                    maxValue to startTime
                }

        val max = fftResults.maxByOrNull { it.first } ?: (0.0 to 0.0)
        val firstPeak = fftResults.indexOfFirst { it.first == max.first }

        return StartingPosition(
            firstSample = firstPeak * STEP_SIZE + samplesToSkip,
            startInSec = max.second,
        )
    }

    companion object {
        const val ANALYZING_DURATION = 2.0
        const val FFT_SAMPLES = 1024
        const val STEP_SIZE = 128
        private val logger = KotlinLogging.logger {}
    }
}
