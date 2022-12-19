package de.tu_chemnitz.fft

import java.util.stream.Stream
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin
import kotlin.test.assertTrue
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.kotlinmath.Complex
import org.kotlinmath.I
import org.kotlinmath.R
import org.kotlinmath.sqrt

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FFTTest {

    @ParameterizedTest
    @MethodSource("getFFTValues")
    fun `fft should yield correct results`(input: List<Double>, expected: List<Complex>) {
        val actual = Main.fft(input)

        actual.forEachIndexed { index, complex ->
            assertTrue(
                complex.closeTo(expected[index]),
                "Expected ${expected[index]} at index $index, but was $complex."
            )
        }
    }

    @ParameterizedTest
    @MethodSource("getFFTValues")
    fun `fft and dft should return same results`(input: List<Double>, expected: List<Complex>) {
        val fftResults = Main.fft(input)
        val dftResults = Main.r2cDft(input)

        fftResults.forEachIndexed { index, fft ->
            assertTrue(
                fft.closeTo(dftResults[index]),
                "Value $fft at index $index is not the same as ${dftResults[index]}."
            )
        }
    }

    private fun abs(n: Complex): Complex = sqrt(n.re * n.re + n.im * n.im)

    private fun signal(frequency: Int, amplitude: Double, sampleRate: Int): List<Double> {
        return (0 until sampleRate).asSequence()
            .map { index -> index.toDouble() / sampleRate.toDouble() }
            .map { t -> amplitude * sin(PI * 2.0 * frequency * t) }
            .toList()
    }

    @ParameterizedTest
    @ValueSource(ints = [20, 200, 2000])
    fun `should yield correct values for sine signal`(frequency: Int) {
        val sampleRate = 4096
        val amplitude = 2.0.pow(15)
        val signal = signal(frequency, amplitude, sampleRate)

        val result = Main.fft(signal)
        val actual = (result[frequency]).let { abs(it).re / (sampleRate / 2) }

        assertTrue(
            amplitude.closeTo(actual),
            "Expected $amplitude, but was $actual."
        )
    }

    private fun Complex.closeTo(number: Complex, e: Double = 0.00001): Boolean =
        abs(re - number.re) < e && abs(im - number.im) < e

    private fun Double.closeTo(number: Double, e: Double = 0.00001): Boolean =
        abs(this - number) < e

    companion object {
        @JvmStatic
        fun getFFTValues(): Stream<Arguments> = Stream.of(
            Arguments.of(listOf(1.0, 2.0, 3.0, 4.0), listOf(10.R, (-2).R + 2.I, (-2).R, (-2).R + (-2).I)),
            Arguments.of(
                listOf(0.0, -12539.770711739264, 23170.47500592079, -30273.684521329826),
                listOf(
                    (-19642.980227).R, (-23170.475006).R - 17733.913810.I,
                    65983.930239.R, (-23170.475006).R + 17733.913810.I
                )
            ),
            Arguments.of(
                listOf(2.0, 1.0, -1.0, 5.0, 0.0, 3.0, 0.0, -4.0),
                listOf(
                    6.R, (-5.778175).R - 3.949747.I, 3.R - 3.I, 9.778175.R - 5.949747.I,
                    (-4).R, 9.778175.R + 5.949747.I, 3.R + 3.I, (-5.778175).R + 3.949747.I
                )
            )
        )
    }
}