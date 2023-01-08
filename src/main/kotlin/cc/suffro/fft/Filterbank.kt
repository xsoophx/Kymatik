package cc.suffro.fft

import cc.suffro.fft.data.FFTData
import cc.suffro.fft.data.FmtChunk
import cc.suffro.fft.data.Window
import cc.suffro.fft.data.hanningFunction
import kotlin.math.abs
import kotlin.math.roundToInt
import org.kotlinmath.Complex

typealias SeparatedSignals = Map<Interval, List<Complex>>

class Filterbank(private val fftProcessor: FFTProcessor) {
    fun separateSignals(fftData: Sequence<FFTData>, fmtChunk: FmtChunk): Sequence<SeparatedSignals> {
        val firstElement = fftData.first()
        val bins = getFrequencyBands(fmtChunk.sampleRate.toDouble()).map {
            Interval(firstElement.binIndexOf(it.first), firstElement.binIndexOf(it.second))
        }

        return fftData.map { data ->
            bins.associateWith { interval ->
                data.output.drop(interval.lowerBound).take(interval.upperBound - interval.lowerBound)
            }
        }
    }

    fun lowPassFilter(windows: Sequence<Window>, fmtChunk: FmtChunk) {
        windows.map(::fullWaveRectify).map { convolve(it, fmtChunk) }
    }

    private fun fullWaveRectify(window: Window) = Window(window.map(::abs), window.intervalTime)

    private fun convolve(window: Window, fmtChunk: FmtChunk): Sequence<Sequence<Double>> {
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

    private fun getFrequencyBands(sampleRate: Double) =
        generateSequence(0.0 to 200.0) { it.second to it.second * 2 }
            .take(7)
            .toMutableList()
            .also { it += it.last().second to sampleRate }


}

data class Interval(
    val lowerBound: Int,
    val upperBound: Int
)
