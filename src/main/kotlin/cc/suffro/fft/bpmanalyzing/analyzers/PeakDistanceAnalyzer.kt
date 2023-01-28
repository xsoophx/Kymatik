package cc.suffro.fft.bpmanalyzing.analyzers

import cc.suffro.fft.bpmanalyzing.data.Bpm
import cc.suffro.fft.fft.FFTProcessor
import cc.suffro.fft.fft.data.FFTData
import cc.suffro.fft.fft.data.WindowFunction
import cc.suffro.fft.wav.data.Wav
import java.lang.StrictMath.abs
import java.lang.StrictMath.min
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class PeakDistanceAnalyzer(private val fftProcessor: FFTProcessor = FFTProcessor()) : BpmAnalyzer {

    fun analyze(
        wav: Wav,
        start: Double = 0.0,
        end: Double = 10.0,
        interval: Double = 0.01,
        windowFunction: WindowFunction? = null
    ): Bpm {
        val windows = wav.getWindows(start = start, end = min(wav.timestampLastSample, end), interval = interval)

        val averagePeakTimes = fftProcessor
            .process(windows, samplingRate = wav.sampleRate, windowFunction = windowFunction)
            .getBassFrequencyBins(interval)
            .getIntervalsOverTime()
            .getAveragePeakTimes()

        val averagePeakDistance = averagePeakTimes.map { it.sorted().getAveragePeakDistance() }.average()
        return (60 / averagePeakDistance).round()
    }

    private fun Sequence<FFTData>.getBassFrequencyBins(interval: Double): Sequence<Peak> {
        val firstElement = first()
        val lowerFrequencyBin = maxOf(0, firstElement.binIndexOf(LOWER_FREQUENCY_BOUND))
        val higherFrequencyBin = minOf(firstElement.binIndexOf(HIGHER_FREQUENCY_BOUND), firstElement.bins.count - 1)

        return mapIndexed { index, fftData ->
            Peak(
                midPoint = index * interval,
                interval = interval,
                values = fftData.magnitudes.subList(lowerFrequencyBin, higherFrequencyBin + 1)
            )
        }
    }

    private fun Sequence<Peak>.getIntervalsOverTime(): List<List<PeakInterval>> =
        first().values.indices.map { bin ->
            map { peak ->
                PeakInterval(midPoint = peak.midPoint, magnitude = peak.values[bin])
            }.toList()
        }

    private fun List<List<PeakInterval>>.getAveragePeakTimes(): List<List<Double>> {
        val averagePeaks = List(size) { mutableListOf<Double>() }

        forEachIndexed { i, intervals ->
            val maxMagnitude = intervals.maxOf { it.magnitude }
            intervals
                .filter { it.magnitude > maxMagnitude / 2 }
                .sortedByDescending { it.magnitude }
                .forEach { interval ->
                    addPeakTime(averagePeaks[i], interval.midPoint)
                }
        }
        return averagePeaks
    }

    private fun addPeakTime(peakTimes: MutableList<Double>, time: Double) {
        val e = 0.05
        val index = peakTimes.indexOfFirst { (abs(it - time)) < e }

        if (index != -1) {
            peakTimes[index] = (peakTimes[index] + time) / 2
        } else if (peakTimes.all { abs(it - time) > MAX_PEAK_DISTANCE }) {
            peakTimes += time
        }
    }

    private fun List<Double>.getAveragePeakDistance(): Double =
        asSequence().zipWithNext().map { (current, next) -> next - current }.average()

    private fun Double.round(): Double =
        DecimalFormat("#.#", DecimalFormatSymbols(Locale.US))
            .apply {
                roundingMode = RoundingMode.CEILING
            }
            .format(this)
            .toDouble()

    private data class Peak(
        val midPoint: Double,
        val interval: Double,
        val values: List<Double>
    )

    private data class PeakInterval(
        val midPoint: Double,
        val magnitude: Double
    )

    companion object {
        private const val LOWER_FREQUENCY_BOUND = 40.0
        private const val HIGHER_FREQUENCY_BOUND = 120.0
        private const val MAX_PEAK_DISTANCE = 60.0 / 220.0
    }
}
