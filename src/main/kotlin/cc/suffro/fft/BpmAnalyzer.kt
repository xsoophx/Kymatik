package cc.suffro.fft

import cc.suffro.fft.data.FFTData
import java.nio.file.Path
import org.kotlinmath.complex

typealias Magnitudes = List<Double>
typealias Deviations = List<Double>

object BpmAnalyzer {
    private const val LOWER_FREQUENCY_BOUND = 40.0
    private const val HIGHER_FREQUENCY_BOUND = 120.0

    fun analyze(path: Path): Double {
        val wav = WAVReader.read(path)
        val windows = wav.getWindows(end = 1.0, interval = 0.01).map { window -> window.map { complex(it, 0) } }

        val bassFrequencyBins = FFTProcessor(windows).process(samplingRate = wav.sampleRate).getBassFrequencyBins()
        val deviations = bassFrequencyBins.toList().getDeviations()

        return 0.0
    }

    fun Sequence<FFTData>.getBassFrequencyBins(): Sequence<Magnitudes> {
        val lowerFrequencyBin = maxOf(0, first().binIndexOf(LOWER_FREQUENCY_BOUND))
        val higherFrequencyBin = minOf(first().binIndexOf(HIGHER_FREQUENCY_BOUND), first().bins.count)

        return map { fftData -> fftData.magnitudes.toList().subList(lowerFrequencyBin, higherFrequencyBin + 1) }
    }

    fun List<Magnitudes>.getDeviations(): List<Deviations> {
        return (0 until size - 1).map { index -> this[index].getDeviation(this[index + 1]) }
    }

    private fun Magnitudes.getDeviation(next: Magnitudes): Deviations {
        return mapIndexed { index, magnitude -> next[index] - magnitude }
    }
}