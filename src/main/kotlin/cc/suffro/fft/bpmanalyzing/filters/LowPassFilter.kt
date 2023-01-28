package cc.suffro.fft.bpmanalyzing.filters

import cc.suffro.fft.bpmanalyzing.data.Signal
import cc.suffro.fft.fft.FFTProcessor
import cc.suffro.fft.fft.data.Window
import cc.suffro.fft.fft.data.hanningFunction
import cc.suffro.fft.getHighestPowerOfTwo
import cc.suffro.fft.wav.data.FmtChunk
import org.kotlinmath.Complex
import kotlin.math.abs
import kotlin.math.roundToInt

class LowPassFilter(private val fftProcessor: FFTProcessor) {

    fun process(window: Window, fmtChunk: FmtChunk): Signal {
        val fullWaveRectified = Window(window.map(::abs), window.duration)
        val numSamples = (window.duration * 2 * fmtChunk.sampleRate).roundToInt()
        val halfHanningWindow = getHalfOfHanningWindow(numSamples)
        val (first, second) = processSignals(fullWaveRectified, halfHanningWindow, fmtChunk.sampleRate)

        val convolved = convolve(first, second)
        return fftProcessor.processInverse(convolved)
    }

    private fun processSignals(
        a: Sequence<Double>,
        b: Sequence<Double>,
        sampleRate: Int
    ): Pair<List<Complex>, List<Complex>> {
        val size = getSmallerSizeOf(a.count(), b.count())
        val first = fftProcessor.process(a.take(size), sampleRate).output
        val second = fftProcessor.process(b.take(size), sampleRate).output
        return first to second
    }

    private fun convolve(
        a: List<Complex>,
        b: List<Complex>,
        block: (Pair<Complex, Complex>) -> Complex = { it.first * it.second }
    ): Sequence<Complex> {
        return a.zip(b).map(block).asSequence()
    }

    private fun getSmallerSizeOf(a: Int, b: Int): Int =
        minOf(getHighestPowerOfTwo(a), getHighestPowerOfTwo(b))

    private fun getHalfOfHanningWindow(numSamples: Int): Sequence<Double> =
        (0 until numSamples)
            .map { hanningFunction(it, numSamples) }
            .subList(numSamples / 2, numSamples)
            .asSequence()

    private fun FFTProcessor.processInverse(signal: Sequence<Complex>): Sequence<Double> =
        processInverse(sequenceOf(signal)).first()
}
