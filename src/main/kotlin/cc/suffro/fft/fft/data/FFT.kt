package cc.suffro.fft.fft.data

import kotlin.math.roundToInt
import org.kotlinmath.Complex
import org.kotlinmath.sqrt

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

    // index = Frequency * Number of FFT Points / Sampling Frequency
    fun binIndexOf(frequency: Double): Int = (frequency * sampleSize / samplingRate).roundToInt()
}

enum class Method {
    FFT,
    FFT_IN_PLACE,
    R2C_DFT
}

data class Window(
    val samples: Sequence<Sample>,
    val intervalTime: Double
) : Sequence<Sample> by samples