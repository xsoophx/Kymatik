package cc.suffro.fft.bpm_analzying.filters

import cc.suffro.fft.bpm_analzying.data.Interval
import cc.suffro.fft.bpm_analzying.data.SeparatedSignals
import cc.suffro.fft.fft.data.FFTData

object Filterbank {
    fun separateSignals(fftData: FFTData, maximumFrequency: Int): SeparatedSignals {
        val bins = getFrequencyBands(fftData.samplingRate.toDouble(), maximumFrequency).map {
            Interval(fftData.binIndexOf(it.first), fftData.binIndexOf(it.second))
        }

        return bins.associateWith { interval ->
            fftData.output.drop(interval.lowerBound).take(interval.upperBound - interval.lowerBound)
        }
    }

    private fun getFrequencyBands(sampleRate: Double, maximumFrequency: Int) =
        generateSequence(0.0 to 200.0) { it.second to it.second * 2 }
            .takeWhile { it.second <= maximumFrequency }
            .toMutableList()
            .also { it += it.last().second to sampleRate }

}
