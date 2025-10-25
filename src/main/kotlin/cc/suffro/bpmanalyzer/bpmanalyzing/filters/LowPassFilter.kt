package cc.suffro.bpmanalyzer.bpmanalyzing.filters

import cc.suffro.bpmanalyzer.bpmanalyzing.data.Signal
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FFTData
import cc.suffro.bpmanalyzer.fft.data.TimeDomainWindow
import cc.suffro.bpmanalyzer.fft.data.WindowFunctionType
import cc.suffro.bpmanalyzer.fft.data.hanningFunction
import cc.suffro.bpmanalyzer.getHighestPowerOfTwo
import cc.suffro.bpmanalyzer.wav.data.FmtChunk
import org.kotlinmath.Complex
import org.kotlinmath.complex
import kotlin.math.abs
import kotlin.math.roundToInt

object LowPassFilter {
    /**
     * Returns the low-pass filtered signal in the time domain.
     */
    fun process(
        window: TimeDomainWindow,
        sampleRate: Int,
    ): Signal {
        val fullWaveRectified = TimeDomainWindow(window.map(::abs), window.duration, window.startingTime)
        val numSamples = (window.duration * 2 * sampleRate).roundToInt()
        val halfHanningWindow = getHalfOfHanningWindow(numSamples)

        val fftSize = getSmallerSizeOf(fullWaveRectified.count(), halfHanningWindow.count())
        val (first, second) = processSignals(fullWaveRectified, halfHanningWindow, sampleRate, fftSize)

        val convolved = convolve(first, second)
        return FFTProcessor.processInverse(convolved)
    }

    fun process(
        window: TimeDomainWindow,
        fmtChunk: FmtChunk,
    ) = process(window, fmtChunk.sampleRate)

    fun simpleLowpass(
        threshold: Double = 100.0,
        samples: List<Double>,
        samplingRate: Int,
    ): FFTData {
        val fftResult =
            FFTProcessor.process(samples, samplingRate, windowFunction = WindowFunctionType.HANNING.function)
        val cutOffIndex = fftResult.binIndexOf(threshold)

        return fftResult.copy(
            output =
                fftResult.output.mapIndexed { index, complex ->
                    if (index <= cutOffIndex) complex else complex(0.0, 0.0)
                },
        )
    }

    private fun processSignals(
        a: Sequence<Double>,
        b: Sequence<Double>,
        sampleRate: Int,
        fftSize: Int,
    ): Pair<List<Complex>, List<Complex>> {
        val first = FFTProcessor.process(a.take(fftSize), sampleRate).output
        val second = FFTProcessor.process(b.take(fftSize), sampleRate).output
        return first to second
    }

    private fun convolve(
        a: List<Complex>,
        b: List<Complex>,
        block: (Pair<Complex, Complex>) -> Complex = { it.first * it.second },
    ): Sequence<Complex> {
        return a.zip(b).map(block).asSequence()
    }

    private fun getSmallerSizeOf(
        a: Int,
        b: Int,
    ): Int = minOf(getHighestPowerOfTwo(a), getHighestPowerOfTwo(b))

    private fun getHalfOfHanningWindow(numSamples: Int): Sequence<Double> =
        (0 until numSamples)
            .map { hanningFunction(it, numSamples) }
            .subList(numSamples / 2, numSamples)
            .asSequence()

    private fun FFTProcessor.processInverse(signal: Sequence<Complex>): Sequence<Double> = processInverse(sequenceOf(signal)).first()
}
