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

        val timeDomainWindows =
            fftData.mapIndexed { index, fftData ->
                // TODO: normalize
                val fullSpectrum = FFTProcessor.buildFullSpectrum(fftData.output)
                val samples = FFTProcessor.processInverse(fullSpectrum)
                val startTime = startingTime + (index * STEP_SIZE) / data.sampleRate.toDouble()
                val startIndex = index * STEP_SIZE + startingSample
                TimeDomainWindow(samples, fftData.duration, startTime, startIndex)
            }

        logger.info { "Calculating indices for sample" }
        val overlapCount : Int = (FFT_SAMPLES + STEP_SIZE - 1) / STEP_SIZE
        return (0 until sampleSizeToAnalyze).map { index ->
            // Windows to consider are those where [start ; end] is within the sample index
            // The maximum number of overlapping windows is overlapCount
            // Graphical example:
            //
            // W0: |########
            // W1: |    ########
            // W2: |        ########
            // W3: |            ########
            // S : |      ^
            //
            // In this example, at sample index S, we have to consider W1 and W2
            // We start with the earliest possible window that could contain the sample (perhaps negative)
            // Then we check each overlapping window to see if it contains the sample
            // With a maximum of overlapCount windows to consider
            // All invalid windows are filtered out (set to index -1 first)
            val allIndicesGlobal = (0 until overlapCount).map { overlapIndex ->
                val firstPossibleWindow = (index - FFT_SAMPLES + STEP_SIZE) / STEP_SIZE
                val thisWindowIndex = firstPossibleWindow + overlapIndex
                if (thisWindowIndex * STEP_SIZE <= index && index < thisWindowIndex * STEP_SIZE + FFT_SAMPLES) {
                    thisWindowIndex
                } else {
                    -1
                }
            }.filter{ it >= 0 && it < timeDomainWindows.size}.ifEmpty {
                listOf(timeDomainWindows.size -1)
            }.toList().distinct()

            val allIndicesLocal = allIndicesGlobal.map { idxGlobal ->
                index - (idxGlobal * STEP_SIZE)
            }

            allIndicesGlobal.mapIndexed { i, idxGlobal ->
                val idxLocal = allIndicesLocal[i]
                timeDomainWindows[idxGlobal].samples.elementAt(idxLocal)
            }.average()
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
