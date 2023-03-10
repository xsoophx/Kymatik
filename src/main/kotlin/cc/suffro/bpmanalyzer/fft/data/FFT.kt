package cc.suffro.bpmanalyzer.fft.data

import cc.suffro.bpmanalyzer.abs
import org.kotlinmath.Complex
import kotlin.math.roundToInt

typealias Sample = Double

@JvmInline
value class Bins(private val bins: Int) {
    init {
        require((bins != 0) && bins and (bins - 1) == 0)
    }

    val count: Int
        get() = bins
}

data class FFTData(
    val bins: Bins,
    val sampleSize: Int,
    val samplingRate: Int,
    val output: List<Complex>
) {
    val magnitudes: List<Double>
        get() = output.take(bins.count).map { abs(it) / sampleSize }

    val duration: Double
        get() = sampleSize.toDouble() / samplingRate

    // index = Frequency * Number of FFT Points / Sampling Frequency
    fun binIndexOf(frequency: Double): Int = (frequency * sampleSize / samplingRate).roundToInt()

    fun binIndexOf(frequency: Int): Int = binIndexOf(frequency.toDouble())
}

enum class Method {
    FFT,
    FFT_IN_PLACE,
    R2C_DFT
}

// TODO: think about better solution
data class TimeDomainWindow(
    val samples: Sequence<Sample>,
    val duration: Double,
    val startingTime: Double
) : Sequence<Sample> by samples

data class FrequencyDomainWindow(
    val magnitudes: List<Double>,
    val startingTime: Double
)
