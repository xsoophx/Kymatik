package cc.suffro.fft.bpmanalyzing.filters

import cc.suffro.fft.bpmanalyzing.data.Signal
import cc.suffro.fft.fft.FFTProcessor
import cc.suffro.fft.fft.data.FFTData
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
        return convolve(fullWaveRectified, fmtChunk)
    }

    private fun convolve(window: Window, fmtChunk: FmtChunk): Signal {
        val samples = (window.duration * 2 * fmtChunk.sampleRate).roundToInt()

        val halfHanningWindow = (0 until samples)
            .map { hanningFunction(it, samples) }
            .subList(samples / 2, samples)

        val size = minOf(
            getHighestPowerOfTwo(halfHanningWindow.size),
            getHighestPowerOfTwo(window.count())
        )

        val transformedHanningWindow =
            fftProcessor.process(halfHanningWindow.asSequence().take(size), fmtChunk.sampleRate).output
        val transformedSignal = fftProcessor.process(window.take(size), fmtChunk.sampleRate).output

        val convolved = transformedHanningWindow.zip(transformedSignal)
            .map { it.first * it.second }.asSequence()

        return fftProcessor.processInverse(convolved)
    }

    private fun FFTProcessor.process(signal: Sequence<Double>, sampleRate: Int): FFTData =
        process(sequenceOf(signal), sampleRate).first()

    private fun FFTProcessor.processInverse(signal: Sequence<Complex>): Sequence<Double> =
        processInverse(sequenceOf(signal)).first()
}
