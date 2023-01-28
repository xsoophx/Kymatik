package cc.suffro.fft.bpmanalyzing.filters

import cc.suffro.fft.bpmanalyzing.data.Interval
import cc.suffro.fft.bpmanalyzing.data.SeparatedSignals
import cc.suffro.fft.fft.data.FFTData
import org.kotlinmath.R

object Filterbank {
    fun separateSignals(fftData: FFTData, maximumFrequency: Int): SeparatedSignals {
        val frequencies = fftData.getFrequencyBands(maximumFrequency)
        val output = MutableList(frequencies.size) { MutableList(fftData.output.size) { 0.R } }

        frequencies.forEachIndexed { index, interval ->

            (interval.lowerBound until interval.upperBound).forEach { value ->
                output[index][value] = fftData.output[value]
            }

            (fftData.sampleSize - interval.upperBound until fftData.sampleSize - interval.lowerBound).map { value ->
                output[index][value] = fftData.output[value]
            }
        }

        return frequencies.mapIndexed { index, interval -> interval to output[index] }.toMap()
    }

    private fun FFTData.getFrequencyBands(maximumFrequency: Int): List<Interval> {
        val limits = generateSequence(0 to 200) { it.second to it.second * 2 }
            .takeWhile { it.second <= maximumFrequency }
            .map { (lower, upper) ->
                Interval(binIndexOf(lower), binIndexOf(upper))
            }

        return limits.toMutableList().apply {
            this += Interval(limits.last().upperBound, binIndexOf(maximumFrequency) - 1)
        }
    }
}