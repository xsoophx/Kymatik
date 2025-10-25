package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.AnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.Analyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.LowPassFilter
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FFTData
import cc.suffro.bpmanalyzer.fft.data.TimeDomainWindow
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path

class StartingPositionAnalyzer(
    private val analyzer: Analyzer<Wav, TrackInfo>,
    private val wavReader: FileReader<Wav>,
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
        require(sampleSizeToAnalyze >= FFT_SAMPLES) {
            "Sample size to analyze must be greater than FFT samples size of $FFT_SAMPLES to execute analyzing."
        }

        val startingTime = startingSample / data.sampleRate.toDouble()
        val fftData = getFFTWindows(samples, sampleSizeToAnalyze, data, startingTime, startingSample)
        val size = fftData.size

        val timeDomainWindows =
            fftData.mapIndexed { index, fftData ->
                logger.info { "Creating Timedomainwindow $index of $size" }

                // TODO: normalize
                val samples = FFTProcessor.processInverse(fftData.output)
                val startTime = startingTime + (index * STEP_SIZE) / data.sampleRate.toDouble()
                val startIndex = index * STEP_SIZE + startingSample
                TimeDomainWindow(samples, fftData.duration, startTime, startIndex)
            }

        return (0 until sampleSizeToAnalyze).map { index ->
            logger.info { "calculating index $index" }
            val firstWindowIndex = index / FFT_SAMPLES
            val firstLocalIndex = index % FFT_SAMPLES
            val firstSamples = timeDomainWindows[firstWindowIndex].samples.toList()

            val secondWindowIndex = (index + STEP_SIZE) / FFT_SAMPLES
            val secondLocalIndex = (index + STEP_SIZE) % FFT_SAMPLES
            val secondSamples = timeDomainWindows[secondWindowIndex].samples.toList()

            if (secondWindowIndex < timeDomainWindows.size) {
                // non overlapping windows
                if (firstWindowIndex == secondWindowIndex) {
                    firstSamples[firstLocalIndex]
                } else {
                    // overlapping windows
                    (firstSamples[firstLocalIndex] + secondSamples[secondLocalIndex]) / 2.0
                }
            } else {
                // last window
                firstSamples[firstLocalIndex]
            }
        }
    }

    private fun getFFTWindows(
        samples: List<Double>,
        sampleSizeToAnalyze: Int,
        data: Wav,
        startingTime: Double,
        startingSample: Int,
    ): List<FFTData> {
        return samples
            .take(sampleSizeToAnalyze)
            .windowed(FFT_SAMPLES, STEP_SIZE, partialWindows = false)
            .map { window ->
                val duration = window.size / data.sampleRate.toDouble()
                val tdWindow = TimeDomainWindow(window, duration, startingTime, startingSample)

                LowPassFilter.simpleLowpass(100.0, tdWindow.samples.toList(), data.sampleRate)
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
