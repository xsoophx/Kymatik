package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.AnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.Analyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.CombFilterOperations
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.DifferentialRectifier
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.LowPassFilter
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FFTData
import cc.suffro.bpmanalyzer.getHighestPowerOfTwo
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import mu.KotlinLogging
import java.nio.file.Path

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

    private fun analyze(
        data: Wav,
        bpm: Bpm,
        samplesToSkip: Int = 0,
    ): StartingPosition {
        logger.info { "Analyzing starting position of track: ${data.filePath} with bpm: $bpm" }
        val sampleSizeToAnalyze = getHighestPowerOfTwo((SILENCE_ANALYZING_DURATION * data.sampleRate).toInt())
        val samples = data.defaultChannel().drop(samplesToSkip).take(sampleSizeToAnalyze).toDoubleArray()

        val fftResults =
            samples.asSequence().windowed(FFT_SAMPLES, STEP_SIZE, partialWindows = true) {
                fftProcessor.process(it, data.sampleRate).magnitudes.sum()
            }.toList()

        return StartingPosition(0, 0.0)
    }

    private fun differentialsOf(
        fftResult: FFTData,
        data: Wav,
    ): List<List<Double>> {
        val signals = combFilterOperations.getFrequencyBands(fftResult)
        val lowPassFiltered =
            signals.map {
                LowPassFilter().process(it, data.fmtChunk)
            }

        return lowPassFiltered.map { DifferentialRectifier.process(it).toList() }.toList()
    }

    private fun sumOfFrequencyEnergies(differentials: List<List<Double>>): List<Double> {
        val differentialSizeX = differentials.first().size
        val differentialSizeY = differentials.size
        val frequencySum = MutableList(differentialSizeX) { 0.0 }

        for (i in 0 until differentialSizeX) {
            for (j in 0 until differentialSizeY) {
                frequencySum[i] += differentials[j][i]
            }
        }
        return frequencySum
    }

    private fun multiplySignals(
        frequencySum: List<Double>,
        size: Int,
        bpm: Bpm,
        data: Wav,
    ): DoubleArray {
        val filledFilter = combFilterOperations.getFilledFilter(size, bpm, data.sampleRate)
        val result = DoubleArray(size)

        for (i in frequencySum.indices) {
            val slice = frequencySum.subList(i, frequencySum.size)

            val sum =
                slice.mapIndexed { index, value ->
                    val product = value * filledFilter[index]
                    product * product
                }.sum()

            result[i] = sum
        }
        return result
    }

    // Just experimental for now
    private fun getBassProfileUntilSeconds(
        seconds: Double = SILENCE_ANALYZING_DURATION,
        data: Wav,
    ): Int {
        val lowPassFilter = LowPassFilter()

        val bassProfile =
            data.defaultChannel().take(131072)
                .let { samples ->
                    val fftResult = fftProcessor.process(samples, data.sampleRate)
                    val bassBand = combFilterOperations.getBassBand(fftResult)
                    // val lowPassFiltered = lowPassFilter.process(bassBand, data.fmtChunk)
                    DifferentialRectifier.process(bassBand)
                }

        val max = bassProfile.max()
        val skipUntil = bassProfile.indexOfFirst { it > 0.001 }

        return skipUntil
    }

    companion object {
        const val SILENCE_ANALYZING_DURATION = 2.0
        const val FFT_SAMPLES = 1024
        const val STEP_SIZE = 512
        private val logger = KotlinLogging.logger {}
    }
}
