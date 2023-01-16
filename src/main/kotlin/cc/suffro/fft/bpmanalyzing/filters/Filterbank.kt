package cc.suffro.fft.bpmanalyzing.filters

import cc.suffro.fft.bpmanalyzing.data.Interval
import cc.suffro.fft.bpmanalyzing.data.SeparatedSignals
import cc.suffro.fft.fft.data.FFTData

object Filterbank {
    fun separateSignals(fftData: FFTData, maximumFrequency: Int): SeparatedSignals {
        val frequencies = fftData.getFrequencyBands(maximumFrequency)

        return frequencies.associate { interval ->
            val firstHalf = fftData.output.subList(interval.lowerBound, interval.upperBound)
            val secondHalf = fftData.output.subList(
                fftData.sampleSize - interval.upperBound,
                fftData.sampleSize - interval.lowerBound
            )
            Interval(interval.lowerBound, interval.upperBound) to firstHalf + secondHalf
        }
    }

    private fun FFTData.getFrequencyBands(maximumFrequency: Int): List<Interval> {
        val limits = generateSequence(0 to 200) { it.second to it.second * 2 }
            .takeWhile { it.second <= maximumFrequency }
            .map { (lower, upper) ->
                Interval(binIndexOf(lower), binIndexOf(upper) - 1)
            }

        return limits.toMutableList().apply {
            this += Interval(limits.last().upperBound, binIndexOf(maximumFrequency))
        }
    }
}
