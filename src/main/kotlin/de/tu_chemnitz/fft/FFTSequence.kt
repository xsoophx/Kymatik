package de.tu_chemnitz.fft

import de.tu_chemnitz.fft.data.Bins
import de.tu_chemnitz.fft.data.FFTData
import de.tu_chemnitz.fft.data.Method
import de.tu_chemnitz.fft.data.Sample
import de.tu_chemnitz.fft.data.Window
import kotlin.math.PI
import org.kotlinmath.Complex
import org.kotlinmath.I
import org.kotlinmath.R
import org.kotlinmath.complex
import org.kotlinmath.exp
import org.kotlinmath.times


class FFTSequence(private val inputSamples: Sequence<Window>) {

    fun process(sampleSize: Int, samplingRate: Double, method: Method = Method.FFT): Sequence<FFTData> {
        return when (method) {
            Method.FFT -> inputSamples.map { fft(it.elements) }
            else -> inputSamples.map { r2cDft(it.elements) }
        }.map {
            FFTData(
                bins = Bins(sampleSize / 2),
                sampleSize = sampleSize,
                samplingRate = samplingRate,
                output = it
            )
        }
    }

    private fun fft(x: Sequence<Sample>): List<Complex> {
        val length = x.count()
        require(length and (length - 1) == 0) { "Length of samples has to be power of two!" }

        if (length <= 1) return listOf(x.first().R)

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
                xn * exp((-2.0).I * PI / length * k * n)
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