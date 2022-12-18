package de.tu_chemnitz.fft

import kotlin.math.sin
import org.kotlinmath.Complex
import org.kotlinmath.I
import org.kotlinmath.cos

object Main {
    private const val pi = 3.14159265359

    @JvmStatic
    fun main(args: Array<String>) {
        println("FFT")
    }

    fun fft(x: List<Complex>): List<Complex> {
        val length = x.size
        if (length == 1) return x

        val pairs = x.chunked(2)
        val odd = fft(pairs.map { it[1] })
        val even = fft(pairs.map { it.first() })

        val resultFirst = mutableListOf<Complex>()
        val resultSecond = mutableListOf<Complex>()

        for (i in 0 until length / 2) {
            resultFirst += even[i] + complexExp(-2.0 * pi / length * i) * odd[i]
            resultSecond += even[i] - complexExp(-2.0 * pi / length * i) * odd[i]
        }

        return resultFirst + resultSecond
    }

    private fun complexExp(d: Double): Complex = cos(d) + I * sin(d)
}