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

        val size = getSmallerSizeOf(halfHanningWindow.count(), fullWaveRectified.count())
        val first = fftProcessor.process(halfHanningWindow.take(size), fmtChunk.sampleRate).output
        val second = fftProcessor.process(fullWaveRectified.take(size), fmtChunk.sampleRate).output

        val convolved = convolve(first, second)
        return fftProcessor.processInverse(convolved)
    }

    private fun convolve(
        a: Collection<Complex>,
        b: Collection<Complex>,
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
