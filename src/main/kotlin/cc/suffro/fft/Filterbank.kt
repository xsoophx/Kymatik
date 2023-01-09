package cc.suffro.fft

import cc.suffro.fft.data.FFTData
import cc.suffro.fft.data.FmtChunk
import cc.suffro.fft.data.Window
import cc.suffro.fft.data.hanningFunction
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import org.kotlinmath.Complex

typealias SeparatedSignals = Map<Interval, List<Complex>>
typealias Signal = Sequence<Double>

class Filterbank(private val fftProcessor: FFTProcessor) {
    fun separateSignals(fftData: FFTData, fmtChunk: FmtChunk): SeparatedSignals {
        val bins = getFrequencyBands(fmtChunk.sampleRate.toDouble()).map {
            Interval(fftData.binIndexOf(it.first), fftData.binIndexOf(it.second))
        }

        return bins.associateWith { interval ->
            fftData.output.drop(interval.lowerBound).take(interval.upperBound - interval.lowerBound)
        }
    }

    fun lowPassFilter(window: Window, fmtChunk: FmtChunk): Sequence<Signal> {
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

    private fun getFrequencyBands(sampleRate: Double) =
        generateSequence(0.0 to 200.0) { it.second to it.second * 2 }
            .take(7)
            .toMutableList()
            .also { it += it.last().second to sampleRate }

    fun differentialRectify(signals: Sequence<Signal>): Sequence<Signal> {
        return signals.map { signal ->
            signal.zipWithNext()
                .map { (current, next) -> next - current }
                .map { d -> max(0.0, abs(d)) }
        }
    }
}

data class Interval(
    val lowerBound: Int,
    val upperBound: Int
)
