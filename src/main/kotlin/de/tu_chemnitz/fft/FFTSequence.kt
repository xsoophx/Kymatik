package de.tu_chemnitz.fft

import de.tu_chemnitz.fft.data.Bins
import de.tu_chemnitz.fft.data.FFTData
import de.tu_chemnitz.fft.data.Method
import de.tu_chemnitz.fft.data.Sample
import kotlin.math.PI
import org.kotlinmath.Complex
import org.kotlinmath.I
import org.kotlinmath.R
import org.kotlinmath.complex
import org.kotlinmath.exp
import org.kotlinmath.times

// r = 1 / T
// 8Hz - T = 125ms
// 16Hz - T = 62.5ms
class FFTSequence(
    private val inputSamples: Sequence<Sample>,
    private val bins: Bins
) {
    fun process(sampleSize: Int, samplingRate: Double, method: Method = Method.FFT): FFTData {
        val fftData = when (method) {
            Method.FFT -> fft(inputSamples)
            else -> r2cDft(inputSamples)
        }
        return FFTData(bins = bins, sampleSize = sampleSize, samplingRate = samplingRate, output = fftData)
    }

    private fun fft(x: Sequence<Sample>): List<Complex> {
        val length = x.count()
        require(length and (length - 1) == 0) { "Length of samples has to be power of two!" }

        if (length <= 1) return listOf(x.first().sample.R)

        val pairs = x.chunked(2)
        val odd = fft(pairs.map { it[1] })
        val even = fft(pairs.map { it.first() })

        val resultFirst = mutableListOf<Complex>()
        val resultSecond = mutableListOf<Complex>()

        for (i in 0 until length / 2) {
            resultFirst += even[i] + exp((-2.0).I * PI / length * i) * odd[i]
            resultSecond += even[i] - exp((-2.0).I * PI / length * i) * odd[i]
        }

        return resultFirst + resultSecond
    }


    private fun r2cDft(x: Sequence<Sample>): List<Complex> {
        val length = x.count()
        return x.mapIndexed { k, _ ->
            x.mapIndexed { n, xn ->
                xn.sample * exp((-2.0).I * PI / length * k * n)
            }.sum { it }
        }.toList()
    }

    private inline fun Sequence<Complex>.sum(selector: (Complex) -> Complex): Complex {
        var result = complex(0, 0)
        for (element in this) {
            result += selector(element)
        }
        return result
    }
}