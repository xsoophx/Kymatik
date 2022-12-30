package cc.suffro.fft

import cc.suffro.fft.data.Method
import cc.suffro.fft.data.Sample
import java.util.stream.Stream
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.kotlinmath.Complex
import org.kotlinmath.I
import org.kotlinmath.R
import org.kotlinmath.complex

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag(FFT)
private class FFTTest {

    @ParameterizedTest
    @MethodSource("getFFTValues")
    fun `fft should yield correct results`(input: Sequence<Sample>, expected: List<Sample>) {
        val fftProcessor = FFTProcessor(inputSamples = sequenceOf(input))
        val actual = fftProcessor.process(samplingRate = DEFAULT_SAMPLING_RATE).first()

        actual.output.forEachIndexed { index, complex ->
            assertTrue(
                complex.closeTo(expected[index]),
                "Expected ${expected[index]} at index $index, but was $complex."
            )
        }
    }

    @ParameterizedTest
    @MethodSource("getFFTValues")
    fun `fft and dft should return same results`(input: Sequence<Sample>, expected: List<Sample>) {
        val fftProcessor = FFTProcessor(inputSamples = sequenceOf(input))

        val fftResults = fftProcessor.process(samplingRate = DEFAULT_SAMPLING_RATE).first()
        val dftResults = fftProcessor.process(samplingRate = DEFAULT_SAMPLING_RATE, Method.R2C_DFT).first()

        fftResults.output.forEachIndexed { index, fft ->
            assertTrue(
                fft.closeTo(dftResults.output.toList()[index]),
                "Value $fft at index $index is not the same as ${dftResults.output.toList()[index]}."
            )
        }
    }

    private fun signal(frequency: Double, amplitude: Double): Sequence<Sample> {
        return (0 until DEFAULT_SAMPLING_RATE).take(DEFAULT_SAMPLE_SIZE).asSequence()
            .map { index -> index.toDouble() / DEFAULT_SAMPLING_RATE }
            .map { t -> amplitude * sin(PI * 2.0 * frequency * t) }
            .map { d -> complex(d, 0) }
    }

    // 44100Hz sampling rate, 22050Hz Band, 1024 FFT Size, 512 Bins, df = 44100Hz/1024 = 43.06Hz
    // samples = windowSize
    // Frequency = index * Sampling Frequency / Number of FFT Points
    // Fs = 44100Hz , N = 1024, Ti = 1s, T = Fs/N = df
    @ParameterizedTest
    @ValueSource(ints = [20, 200, 2000])
    fun `should yield correct values for sine signal`(frequency: Int) {
        val amplitude = 2.0.pow(15)
        val signal = signal(frequency.toDouble(), amplitude)

        val fftProcessor = FFTProcessor(inputSamples = sequenceOf(signal))
        val result = fftProcessor.process(samplingRate = DEFAULT_SAMPLING_RATE).first()

        val magnitudes = result.magnitudes
        val maximumIndex = magnitudes.indexOf(magnitudes.max())
        val binIndex = (frequency * DEFAULT_SAMPLE_SIZE / DEFAULT_SAMPLING_RATE.toDouble()).roundToInt()

        // TODO: some refactoring, extract into separate tests
        assertEquals(expected = binIndex, actual = maximumIndex)
        assertEquals(expected = binIndex, actual = result.binIndexOf(frequency.toDouble()))

    }

    @ParameterizedTest
    @ValueSource(ints = [43, 430, 4306])
    fun `should yield correct magnitudes for sine signal`(frequency: Int) {
        val amplitude = 2.0.pow(15)
        val signal = signal(frequency.toDouble(), amplitude)

        val fftProcessor = FFTProcessor(inputSamples = sequenceOf(signal))
        val result = fftProcessor.process(samplingRate = DEFAULT_SAMPLING_RATE).first()
        val magnitudes = result.magnitudes.toList()

        result.binIndexOf(frequency.toDouble()).let {
            assertTrue(
                //not mathematically correct, but enough for testing purposes
                magnitudes[it].closeTo(amplitude / 2, e = 20.0),
                "Expected index $it with magnitude ${magnitudes[it]} to be close to ${amplitude / 2}."
            )
        }
    }

    private fun Complex.closeTo(number: Complex, e: Double = 2.0): Boolean =
        abs(re - number.re) < e && abs(im - number.im) < e

    private fun Double.closeTo(number: Double, e: Double = 2.0): Boolean =
        abs(this - number) < e

    companion object {
        private const val DEFAULT_SAMPLE_SIZE = 1024
        private const val DEFAULT_SAMPLING_RATE = 44100

        @JvmStatic
        fun getFFTValues(): Stream<Arguments> = Stream.of(
            Arguments.of(sequenceOf(1.0.R, 2.0.R, 3.0.R, 4.0.R), listOf(10.R, (-2).R + 2.I, (-2).R, (-2).R + (-2).I)),
            Arguments.of(
                sequenceOf(0.0.R, (-12539.770711739264).R, 23170.47500592079.R, (-30273.684521329826).R),
                listOf(
                    (-19642.980227).R, (-23170.475006).R - 17733.913810.I,
                    65983.930239.R, (-23170.475006).R + 17733.913810.I
                )
            ),
            Arguments.of(
                sequenceOf(2.0.R, 1.0.R, (-1.0).R, 5.0.R, 0.0.R, 3.0.R, 0.0.R, (-4.0).R),
                listOf(
                    6.R, (-5.778175).R - 3.949747.I, 3.R - 3.I, 9.778175.R - 5.949747.I,
                    (-4).R, 9.778175.R + 5.949747.I, 3.R + 3.I, (-5.778175).R + 3.949747.I
                )
            )
        )
    }
}