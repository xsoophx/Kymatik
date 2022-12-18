package de.tu_chemnitz.fft

import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.kotlinmath.Complex
import org.kotlinmath.I
import org.kotlinmath.R

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FFTTest {

    @Test
    fun `fft should yield correct results`() {
        val input = listOf(1.R, 2.R, 3.R, 4.R)
        val actual = Main.fft(input)
        val expected = listOf(10.R, (-2).R + 2.I, (-2).R, (-2).R + (-2).I)

        actual.forEachIndexed { index, complex ->
            assertTrue(
                complex.closeTo(expected[index]),
                "Expected ${expected[index]}, but was $complex."
            )
        }
    }

    @Test
    fun `fft should yield even more correct results`() {
        val input = listOf(2.R, 1.R, (-1).R, 5.R, 0.R, 3.R, 0.R, (-4).R)
        val actual = Main.fft(input)
        val expected = listOf(6.R, (-9).R - (6).I, 3.R - 3.I, 13.R - 8.I, (-4).R, 13.R + 8.I, 3.R + 3.I, (-9).R + 6.I)

        actual.forEachIndexed { index, complex ->
            assertTrue(
                complex.closeTo(expected[index]),
                "Expected ${expected[index]}, but was $complex."
            )
        }
    }

    private fun Complex.closeTo(number: Complex, e: Double = 0.00001): Boolean =
        kotlin.math.abs(re - number.re) < e && kotlin.math.abs(im - number.im) < e
}