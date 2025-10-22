package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.AnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.Analyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.CombFilterOperations
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.LowPassFilter
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FFTData
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

    override fun getTransformedSamples(
        samples: List<Double>,
        sampleSizeToAnalyze: Int,
        data: Wav,
        startingSample: Int,
    ): List<Double> {
        val startingTime = startingSample / data.sampleRate.toDouble()
        val fftData = getFFTWindows(samples, sampleSizeToAnalyze, data, startingTime, startingSample)

        val timeDomainWindows =
            fftData.mapIndexed { index, fftData ->
                val samples = FFTProcessor.processInverse(fftData)
                val startTime = startingTime + (index * STEP_SIZE) / data.sampleRate.toDouble()
                val startIndex = index * STEP_SIZE + startingSample
                TimeDomainWindow(samples, fftData.duration, startTime, startIndex)
            }

        val paddedValues =
            timeDomainWindows.mapIndexed { index, window ->
                window.fill(
                    index * STEP_SIZE,
                    sampleSizeToAnalyze - window.samples.count(),
                )
            }

        val length = paddedValues.first().size
        val averaged =
            (0 until length).map { i ->
                val column = paddedValues.map { it[i] }

                column.filter { it >= -1.0 }.average()
            }

        return averaged
    }

    private fun TimeDomainWindow.fill(
        leftPadding: Int,
        rightPadding: Int,
    ): List<Double> {
        val left = List(leftPadding) { -2.0 }
        val right = List(rightPadding) { -2.0 }
        return left + this.samples.toList() + right
    }

    private fun getFFTWindows(
        samples: List<Double>,
        sampleSizeToAnalyze: Int,
        data: Wav,
        startingTime: Double,
        startingSample: Int,
    ): List<FFTData> {
        val k = ln(1000.0) / (FFT_SAMPLES - 1) // f(N-1) ca. 1/1000
        val eFunction = (0 until FFT_SAMPLES).map { i -> exp(-k * i) }

        return samples
            .take(sampleSizeToAnalyze)
            .windowed(FFT_SAMPLES, STEP_SIZE, partialWindows = false)
            .map { window ->
                val duration = window.size / data.sampleRate.toDouble()
                val tdWindow = TimeDomainWindow(window, duration, startingTime, startingSample)

                LowPassFilter.getFrequencyDomainWindow(tdWindow, data.sampleRate)
            }
    }

    private fun analyze(
        data: Wav,
        bpm: Bpm,
        samplesToSkip: Int = 0,
    ): StartingPosition {
        logger.info { "Analyzing starting position of track: ${data.filePath} with bpm: $bpm" }
        val sampleSizeToAnalyze = (ANALYZING_DURATION * data.sampleRate).toInt()

        // TODO: this could be streamed to avoid loading all samples in memory
        val samples = data.defaultChannel().drop(samplesToSkip)

        // TODO
        return StartingPosition(
            firstSample = 1,
            startInSec = 1.0,
        )
    }

    companion object {
        const val ANALYZING_DURATION = 2.0
        const val FFT_SAMPLES = 1024
        const val STEP_SIZE = 512
        private val logger = KotlinLogging.logger {}
    }
}
