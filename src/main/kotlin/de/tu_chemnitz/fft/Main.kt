package de.tu_chemnitz.fft

import kotlin.math.PI
import org.kotlinmath.Complex
import org.kotlinmath.I
import org.kotlinmath.R
import org.kotlinmath.complex
import org.kotlinmath.exp
import org.kotlinmath.times

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println("FFT")
    }

    // example values:
    // 44100 sampling rate
    // 1024 / 44100Hz = 0.23 ms / ~43Hz
    // fft size = 1024
    fun fft(x: List<Double>): List<Complex> {
        val length = x.size
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

    fun r2cDft(x: List<Double>): List<Complex> {
        val length = x.size
        return x.mapIndexed { k, _ ->
            x.mapIndexed { n, xn ->
                xn * exp((-2.0).I * PI / length * k * n)
            }.sum { it }
        }
    }

    private inline fun List<Complex>.sum(selector: (Complex) -> Complex): Complex {
        var result = complex(0, 0)
        for (element in this) {
            result += selector(element)
        }
        return result
    }
}