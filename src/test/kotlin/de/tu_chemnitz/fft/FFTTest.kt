package de.tu_chemnitz.fft

import de.tu_chemnitz.fft.data.Method
import de.tu_chemnitz.fft.data.Sample
import de.tu_chemnitz.fft.data.Window
import java.util.stream.Stream
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.test.assertEquals
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
    fun `fft should yield correct results`(input: Sequence<Double>, expected: List<Complex>) {
        val sequence = FFTSequence(inputSamples = sequenceOf(Window(size = sampleSize, elements = input)))
        val actual = sequence.process(sampleSize = sampleSize, samplingRate = samplingRate).first()

        actual.output.forEachIndexed { index, complex ->
            assertTrue(
                complex.closeTo(expected[index]),
                "Expected ${expected[index]} at index $index, but was $complex."
            )
        }
    }

    @ParameterizedTest
    @MethodSource("getFFTValues")
    fun `fft and dft should return same results`(input: Sequence<Double>, expected: List<Complex>) {
        val sequence = FFTSequence(inputSamples = sequenceOf(Window(size = sampleSize, elements = input)))

        val fftResults = sequence.process(sampleSize = sampleSize, samplingRate = samplingRate).first()
        val dftResults = sequence.process(sampleSize = sampleSize, samplingRate = samplingRate, Method.R2C_DFT).first()

        fftResults.output.forEachIndexed { index, fft ->
            assertTrue(
                fft.closeTo(dftResults.output[index]),
                "Value $fft at index $index is not the same as ${dftResults.output[index]}."
            )
        }
    }

    private fun abs(n: Complex): Complex = sqrt(n.re * n.re + n.im * n.im)

    private fun signal(frequency: Double, amplitude: Double): Sequence<Sample> {
        return (0 until samplingRate.toInt()).take(sampleSize).asSequence()
            .map { index -> index.toDouble() / samplingRate }
            .map { t -> amplitude * sin(PI * 2.0 * frequency * t) }
    }

    // 44100Hz sampling rate, 22050Hz Band, 1024 FFT Size, 512 Bins, df = 44100Hz/1024 = 43.06Hz
    // samples = windowSize
    // Frequency = index * Sampling Frequency / Number of FFT Points
    // Fs = 44100Hz , N = 1024, Ti = 1s, T = Fs/N = df
    @ParameterizedTest
    @ValueSource(ints = [20, 200, 2000])
    fun `should yield correct values for sine signal`(frequency: Int) {
        val magnitude = 2.0.pow(15)
        val signal = signal(frequency.toDouble(), magnitude)

        val sequence = FFTSequence(inputSamples = sequenceOf(Window(size = sampleSize, elements = signal)))
        val result = sequence.process(sampleSize = sampleSize, samplingRate = samplingRate).first()

        val amplitudes = result.output.take(bins).map { abs(it).re / sampleSize }
        val maximumIndex = amplitudes.indexOf(amplitudes.max())
        val binIndex = (frequency * sampleSize / samplingRate).roundToInt()

        // TODO: some refactoring, extract into separate tests
        assertEquals(expected = binIndex, actual = maximumIndex)
        assertEquals(expected = binIndex, actual = result.binIndexOf(frequency.toDouble()))

    }

    @ParameterizedTest
    @ValueSource(ints = [43, 430, 4306])
    fun `should yield correct magnitudes for sine signal`(frequency: Int) {
        val magnitude = 2.0.pow(15)
        val signal = signal(frequency.toDouble(), magnitude)

        val sequence = FFTSequence(inputSamples = sequenceOf(Window(size = sampleSize, elements = signal)))
        val result = sequence.process(sampleSize = sampleSize, samplingRate = samplingRate).first()
        val amplitudes = result.output.take(bins).map { abs(it).re / sampleSize }

        result.binIndexOf(frequency.toDouble()).let {
            assertTrue(
                //not mathematically correct, but enough for testing purposes
                amplitudes[it].closeTo(magnitude / 2, e = 20.0),
                "Expected index $it with magnitude ${amplitudes[it]} to be close to ${magnitude / 2}."
            )
        }
    }

    private fun Complex.closeTo(number: Complex, e: Double = 2.0): Boolean =
        abs(re - number.re) < e && abs(im - number.im) < e

    private fun Double.closeTo(number: Double, e: Double = 2.0): Boolean =
        abs(this - number) < e

    companion object {
        private const val sampleSize = 1024
        private const val samplingRate = 44100.0
        private const val bins = sampleSize / 2

        @JvmStatic
        fun getFFTValues(): Stream<Arguments> = Stream.of(
            Arguments.of(sequenceOf(1.0, 2.0, 3.0, 4.0), listOf(10.R, (-2).R + 2.I, (-2).R, (-2).R + (-2).I)),
            Arguments.of(
                sequenceOf(0.0, -12539.770711739264, 23170.47500592079, -30273.684521329826),
                listOf(
                    (-19642.980227).R, (-23170.475006).R - 17733.913810.I,
                    65983.930239.R, (-23170.475006).R + 17733.913810.I
                )
            ),
            Arguments.of(
                sequenceOf(2.0, 1.0, -1.0, 5.0, 0.0, 3.0, 0.0, -4.0),
                listOf(
                    6.R, (-5.778175).R - 3.949747.I, 3.R - 3.I, 9.778175.R - 5.949747.I,
                    (-4).R, 9.778175.R + 5.949747.I, 3.R + 3.I, (-5.778175).R + 3.949747.I
                )
            )
        )
    }
}