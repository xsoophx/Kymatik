package cc.suffro.fft

import cc.suffro.fft.data.Bins
import cc.suffro.fft.data.FFTData
import cc.suffro.fft.data.Method
import cc.suffro.fft.data.Sample
import cc.suffro.fft.data.Window
import kotlin.math.PI
import org.kotlinmath.Complex
import org.kotlinmath.I
import org.kotlinmath.R
import org.kotlinmath.complex
import org.kotlinmath.exp

class FFTProcessor(private val inputSamples: Sequence<Window<Sample>>) {

    fun process(samplingRate: Int, method: Method = Method.FFT_IN_PLACE): Sequence<FFTData> {
        return when (method) {
            Method.FFT -> inputSamples.map(::fft)
            Method.FFT_IN_PLACE -> inputSamples.map(::fftInPlace)
            Method.R2C_DFT -> inputSamples.map(::r2cDft)
        }.map {
            val sampleSize = it.count()
            FFTData(
                bins = Bins(sampleSize / 2),
                sampleSize = sampleSize,
                samplingRate = samplingRate,
                output = it
            )
        }
    }

    private fun Sequence<Complex>.toArray(size: Int): Array<Complex> {
        val iterator = iterator()
        return Array(size) { iterator.next() }
    }

    private fun log2(number: Int): Int {
        return if (number == 0)
            0
        else
            31 - Integer.numberOfLeadingZeros(number)
    }

    // https://en.wikipedia.org/w/index.php?title=Cooley%E2%80%93Tukey_FFT_algorithm#Data_reordering,_bit_reversal,_and_in-place_algorithms
    private fun fftInPlace(x: Sequence<Complex>): Sequence<Complex> {
        val length = x.count()
        require(length and (length - 1) == 0) { "Length of samples has to be power of two, but was $length!" }

        val xA = x.toArray(length)
        val result = Array(length) { xA[it] }
        bitReverseCopy(xA, result)

        for (s in 1 until log2(length) + 1) {
            val m = 1 shl s
            val wm = exp((-2.0).I * PI / m)

            for (k in 0 until length step m) {
                var omega = 1.R
                val mHalf = m ushr 1
                for (j in 0 until mHalf) {
                    val t = omega * result[k + j + mHalf]
                    val u = result[k + j]
                    result[k + j] = u + t
                    result[k + j + mHalf] = u - t
                    omega *= wm
                }
            }
        }
        return result.asSequence()
    }

    // http://www.librow.com/articles/article-10
    private fun bitReverseCopy(input: Array<Complex>, result: Array<Complex>) {
        var target: UInt = 0u
        val length: UInt = input.size.toUInt()

        for (index in 0 until length.toInt()) {
            if (target > index.toUInt()) {
                swap(input, result, target, index)
            }
            var mask: UInt = length
            mask = mask shr 1
            while (target and mask != 0u) {
                target = target and mask.inv()
                mask = mask shr 1
            }
            target = target or mask
        }
    }

    private fun swap(a: Array<Complex>, result: Array<Complex>, rev: UInt, i: Int) {
        val temp = result[rev.toInt()]
        result[rev.toInt()] = a[i]
        result[i] = temp
    }

    private fun fft(x: Sequence<Complex>): Sequence<Complex> {
        val length = x.count()
        require(length and (length - 1) == 0) { "Length of samples has to be power of two!" }

        if (length <= 1) return sequenceOf(x.first())

        val pairs = x.chunked(2)
        val odd = fft(pairs.map { it[1] }).toList()
        val even = fft(pairs.map { it.first() }).toList()

        val resultFirst = mutableListOf<Complex>()
        val resultSecond = mutableListOf<Complex>()

        for (i in 0 until length / 2) {
            resultFirst += even[i] + exp((-2.0).I * PI / length * i) * odd[i]
            resultSecond += even[i] - exp((-2.0).I * PI / length * i) * odd[i]
        }

        return (resultFirst + resultSecond).asSequence()
    }

    private fun r2cDft(x: Sequence<Complex>): Sequence<Complex> {
        val length = x.count()
        return x.mapIndexed { k, _ ->
            x.mapIndexed { n, xn ->
                xn * exp((-2.0).I * PI / length * k * n)
            }.sum { it }
        }
    }

    private inline fun Sequence<Complex>.sum(selector: (Complex) -> Complex): Complex {
        var result = complex(0, 0)
        for (element in this) {
            result += selector(element)
        }
        return result
    }
}