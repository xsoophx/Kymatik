package de.tu_chemnitz.fft

import java.util.stream.Stream
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.kotlinmath.Complex
import org.kotlinmath.I
import org.kotlinmath.R
import org.kotlinmath.complex

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

    @Test
    fun `fft should yield even more correct results`() {
        val input = listOf(2.0, 1.0, -1.0, 5.0, 0.0, 3.0, 0.0, -4.0)
        val actual = Main.fft(input)
        val expected = listOf(
            6.R,
            (-5.778175).R - 3.949747.I,
            3.R - 3.I,
            9.778175.R - 5.949747.I,
            (-4).R,
            9.778175.R + 5.949747.I,
            3.R + 3.I,
            (-5.778175).R + 3.949747.I
        )

        actual.forEachIndexed { index, complex ->
            assertTrue(
                complex.closeTo(expected[index]),
                "Expected ${expected[index]} at index $index, but was $complex."
            )
        }
    }

    private fun signal(amplitude: Double, sampleRate: Int): List<Double> {
        val frequency = 50.0
        return (0 until sampleRate).asSequence()
            .map { index -> index.toDouble() / sampleRate.toDouble() }
            .map { t -> amplitude * sin(PI * 2.0 * frequency * t) }
            .toList()
    }

    @Test
    fun `should yield correct values for sine signal`() {
        val sampleRate = 32
        val amplitude = 2.0.pow(15)

        val signal = signal(amplitude, sampleRate)
        val expected = listOf(
            "-17148.664748,0.000000",
            "-9712.426607,-12399.762691",
            "4798.762497,-11585.237503",
            "10868.130631,2012.392262",
            "1178.498328,14303.518101",
            "-15218.273947,11595.695619",
            "-21182.762497,-4798.762497",
            "-9035.131285,-18249.849867",
            "9597.524994,-12539.770712",
            "12562.626064,10357.186335",
            "-11585.237503,27969.237503",
            "-48856.589875,15123.190398",
            "-66714.498328,-32037.431911",
            "-39475.166833,-87142.012983",
            "27969.237503,414398.762497",
            "98866.831852,-76474.626513",
            "129025.614760,0.000000",
            "98866.831852,76474.626513",
            "27969.237503,-414398.762497",
            "-39475.166833,87142.012983",
            "-66714.498328,32037.431911",
            " -48856.589875,-15123.190398",
            "-11585.237503,-27969.237503",
            "12562.626064,-10357.186335",
            "9597.524994,12539.770712",
            "-9035.131285,18249.849867",
            "-21182.762497,4798.762497",
            "-15218.273947,-11595.695619",
            "1178.498328,-14303.518101",
            "10868.130631,-2012.392262",
            "4798.762497,11585.237503",
            "-9712.426607,12399.762691"
        ).map { expected ->
            expected.split(",").let { complex(it[0].toDouble(), it[1].toDouble()) }
        }

        val result = Main.fft(signal)
        result.forEachIndexed { index, complex ->
            assertTrue(
                complex.closeTo(expected[index]),
                "Expected ${expected[index]} at index $index, but was $complex."
            )
        }
    }

    private fun Complex.closeTo(number: Complex, e: Double = 0.00001): Boolean =
        kotlin.math.abs(re - number.re) < e && kotlin.math.abs(im - number.im) < e

    companion object {
        @JvmStatic
        fun getFFTValues(): Stream<Arguments> = Stream.of(
            Arguments.of(listOf(1.0, 2.0, 3.0, 4.0), listOf(10.R, (-2).R + 2.I, (-2).R, (-2).R + (-2).I)),
            Arguments.of(
                listOf(0.0, -12539.770711739264, 23170.47500592079, -30273.684521329826),
                listOf(
                    (-19642.980227).R,
                    (-23170.475006).R - 17733.913810.I,
                    65983.930239.R,
                    (-23170.475006).R + 17733.913810.I
                )
            ),
            Arguments.of(
                listOf(
                    0.0,
                    -12539.770711739264,
                    23170.47500592079,
                    -30273.684521329826,
                    32768.0,
                    -30273.68452132982,
                    23170.475005920864,
                    -12539.770711739087,
                    -0.000000000016092256040783642,
                    12539.770711739116,
                    -23170.475005920885,
                    30273.68452132983,
                    -32768.0,
                    30273.68452132981,
                    -23170.47500592052,
                    12539.770711739502
                ),
                listOf(
                    0.R + 0.I, (-6583.715217).R,
                    0.R - 0.I, (-7143.747299).R,
                    0.R + 0.I, (-8475.969245).R,
                    0.R + 0.I, (-11206.531312).R,
                    0.R + 0.I, (-17206.306750).R,
                    0.R - 0.I, (-34046.847932).R,
                    0.R - 0.I, (-135697.235572).R,
                    0.R + 262144.I, 220360.353326.R,
                    0.R, 220360.353326.R,
                    (-262144).I, (-135697.235572).R,
                    0.R, (-34046.847932).R,
                    0.R, (-17206.306750).R,
                    0.R, 0.R, (-11206.531312).R,
                    0.R, (-8475.969245).R,
                    0.R, (-7143.747299).R,
                    0.R, (-6583.715217).R
                )
            )
        )
    }
}