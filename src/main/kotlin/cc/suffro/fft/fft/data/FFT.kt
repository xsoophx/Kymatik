package cc.suffro.fft.fft.data

import org.kotlinmath.Complex
import org.kotlinmath.sqrt
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
    private fun abs(n: Complex): Complex = sqrt(n.re * n.re + n.im * n.im)

    val magnitudes: List<Double>
        get() = output.take(bins.count).map { abs(it).re / sampleSize }

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

data class Window(
    val samples: Sequence<Sample>,
    val duration: Double
) : Sequence<Sample> by samples
