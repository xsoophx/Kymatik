package cc.suffro.fft

import cc.suffro.fft.data.FFTData
import cc.suffro.fft.data.FmtChunk
import org.kotlinmath.Complex

typealias SeparatedSignals = Map<Interval, Sequence<Complex>>

object Filterbank {
    fun process(fftData: Sequence<FFTData>, fmtChunk: FmtChunk): Sequence<SeparatedSignals> {
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

    private fun getFrequencyBands(sampleRate: Double) =
        generateSequence(0.0 to 200.0) { it.second to it.second * 2 }
            .take(7)
            .toMutableList()
            .also { it += it.last().second to sampleRate }

    /*
    private fun Sequence<SeparatedSignals>.lowPassFilter() {
        this.map(::fullWaveRectify)
    }

    private fun fullWaveRectify(seperatedSignals: SeparatedSignals) {
    }*/

}

data class Interval(
    val lowerBound: Int,
    val upperBound: Int
)
