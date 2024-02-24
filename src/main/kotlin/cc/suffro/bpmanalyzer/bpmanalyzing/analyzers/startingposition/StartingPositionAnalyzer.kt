package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.AnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.BpmAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.bpmanalyzing.data.getFrequencyBands
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.CombFilter
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.DifferentialRectifier
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.LowPassFilter
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FFTData
import cc.suffro.bpmanalyzer.getHighestPowerOfTwo
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import java.nio.file.Path

class StartingPositionAnalyzer(
    private val analyzer: BpmAnalyzer<CombFilter>,
    private val wavReader: FileReader<Wav>,
    private val combFilter: CombFilter,
    private val fftProcessor: FFTProcessor,
) : CacheAnalyzer<Wav, StartingPosition, StartingPosition> {
    override fun analyze(data: Wav): StartingPosition {
        val bpm = analyzer.analyze(data)
        return analyze(data, bpm)
    }

    override fun getPathAndAnalyze(path: String): StartingPosition {
        return getPathAndAnalyze(Path.of(path))
    }

    override fun getPathAndAnalyze(path: Path): StartingPosition {
        val wav = wavReader.read(path)
        val bpm = analyzer.analyze(wav)
        return analyze(wav, bpm)
    }

    override fun analyze(
        data: Wav,
        params: AnalyzerParams<StartingPosition>,
    ): StartingPosition {
        val bpm = (params as StartingPositionCacheAnalyzerParams).bpm
        return analyze(data, bpm)
    }

    private fun analyze(
        data: Wav,
        bpm: Bpm,
    ): StartingPosition {
        val size = data.defaultChannel().size
        val fftResult = fftOfFirstSamples(bpm, data)
        val differentials = differentialsOf(fftResult, data)
        val frequencySum = sumOfFrequencyEnergies(differentials)

        val result = multiplySignals(frequencySum, size, bpm, data)

        return result.withIndex().maxBy { it.value }.let {
            StartingPosition(it.index, it.index.toDouble() / data.sampleRate)
        }
    }

    private fun fftOfFirstSamples(
        bpm: Bpm,
        data: Wav,
    ): FFTData {
        val firstSamples = combFilter.getRelevantSamples(bpm, data.sampleRate, data.dataChunk.data.first())
        val highestPowerOfTwo = getHighestPowerOfTwo(firstSamples.size)

        return fftProcessor.process(firstSamples.asSequence().take(highestPowerOfTwo), data.sampleRate)
    }

    private fun differentialsOf(
        fftResult: FFTData,
        data: Wav,
    ): List<List<Double>> {
        val signals = fftResult.getFrequencyBands(fftResult.duration, fftProcessor)
        val lowPassFiltered =
            signals.map {
                LowPassFilter(fftProcessor).process(it, data.fmtChunk)
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
        val filledFilter = combFilter.getFilledFilter(size, bpm, data.sampleRate)
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
}
