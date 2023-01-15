package cc.suffro.fft.bpmanalyzing.filters

import cc.suffro.fft.bpmanalyzing.data.Interval
import cc.suffro.fft.bpmanalyzing.data.SeparatedSignals
import cc.suffro.fft.fft.data.FFTData

object Filterbank {
    private val bandLimits = listOf(0, 200, 400, 800, 1600, 3200)

    fun separateSignals(fftData: FFTData): SeparatedSignals {
        val frequencies = fftData.getFrequencyBands()

        return frequencies.associate { interval ->
            val firstHalf = fftData.output.subList(interval.lowerBound, interval.upperBound)
            val secondHalf = fftData.output.subList(
                fftData.sampleSize - interval.upperBound,
                fftData.sampleSize - interval.lowerBound
            )
            Interval(interval.lowerBound, interval.upperBound) to firstHalf + secondHalf
        }
    }

    private fun FFTData.getFrequencyBands(): List<Interval> {
        val limits = bandLimits.zipWithNext { current, next ->
            Interval(binIndexOf(current), binIndexOf(next) - 1)
        }

        return limits.toMutableList().apply {
            this += Interval(binIndexOf(bandLimits.last()), bins.count - 1)
        }
    }
}
