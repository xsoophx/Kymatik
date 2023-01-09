package cc.suffro.fft.filters

import cc.suffro.fft.FFTProcessor
import cc.suffro.fft.data.FFTData
import cc.suffro.fft.data.FmtChunk
import cc.suffro.fft.data.Window
import cc.suffro.fft.data.hanningFunction
import kotlin.math.abs
import kotlin.math.roundToInt

class LowPassFilter(private val fftProcessor: FFTProcessor) {
    fun process(window: Window, fmtChunk: FmtChunk): Sequence<Signal> {
        val fullWaveRectified = Window(window.map(::abs), window.intervalTime)
        return convolve(fullWaveRectified, fmtChunk)
    }

    private fun convolve(window: Window, fmtChunk: FmtChunk): Sequence<Signal> {
        val samples = (window.intervalTime * 2 * fmtChunk.sampleRate).roundToInt()

        val halfHanningWindow = (0 until samples)
            .map { hanningFunction(it, samples) }
            .subList(samples / 2, samples)
            .asSequence()

        val transformedHanningWindow = fftProcessor.process(halfHanningWindow, fmtChunk.sampleRate).output
        val transformedSignal = fftProcessor.process(window, fmtChunk.sampleRate).output

        val convolved = transformedHanningWindow.zip(transformedSignal)
            .map { it.first * it.second }.asSequence()

        return fftProcessor.processInverse(sequenceOf(convolved))
    }

    private fun FFTProcessor.process(signal: Sequence<Double>, sampleRate: Int): FFTData =
        process(sequenceOf(signal), sampleRate).first()
}