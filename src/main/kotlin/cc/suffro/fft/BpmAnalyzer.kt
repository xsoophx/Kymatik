package cc.suffro.fft

import cc.suffro.fft.data.FFTData
import java.lang.StrictMath.abs
import java.nio.file.Path

data class Peak(
    val midPoint: Double,
    val interval: Double,
    val values: List<Double>
)

data class Interval(
    val midPoint: Double,
    val magnitude: Double
)

object BpmAnalyzer {
    private const val LOWER_FREQUENCY_BOUND = 40.0
    private const val HIGHER_FREQUENCY_BOUND = 120.0

    private const val MAX_PEAK_DISTANCE = 60.0 / 220.0

    fun analyze(path: Path): Double {
        val wav = WAVReader.read(path)
        val start = 0.0
        val end = 1.0
        val interval = 0.01

        val windows = wav.getWindows(start = start, end = end, interval = 0.01)


        val bassFrequencyBins =
            FFTProcessor(windows).process(samplingRate = wav.sampleRate).getBassFrequencyBins(interval)
        val intervals = bassFrequencyBins.getIntervalsOverTime()
        val averagePeakTimes = intervals.getAveragePeakTimes()

        val averagePeakDistance = averagePeakTimes.map { it.sorted().getAveragePeakDistance() }.average()
        return 60 / averagePeakDistance
    }

    fun Sequence<FFTData>.getBassFrequencyBins(interval: Double): Sequence<Peak> {
        val lowerFrequencyBin = maxOf(0, first().binIndexOf(LOWER_FREQUENCY_BOUND))
        val higherFrequencyBin = minOf(first().binIndexOf(HIGHER_FREQUENCY_BOUND), first().bins.count - 1)

        return mapIndexed { index, fftData ->
            Peak(
                midPoint = index * interval,
                interval = interval,
                values = fftData.magnitudes.drop(lowerFrequencyBin).take(higherFrequencyBin - lowerFrequencyBin + 1)
                    .toList()
            )
        }
    }

    private fun Sequence<Peak>.getIntervalsOverTime(): List<List<Interval>> =
        (0 until first().values.size).map { bin ->
            map { peak ->
                Interval(midPoint = peak.midPoint, magnitude = peak.values[bin])
            }.toList()
        }.toList()


    private fun List<List<Interval>>.getAveragePeakTimes(): List<List<Double>> {
        val bassFrequencyBins = size
        val averagePeaks = MutableList(bassFrequencyBins) { mutableListOf<Double>() }

        for (i in 0 until bassFrequencyBins) {
            val maxMagnitude = this[i].maxOf { it.magnitude }
            this[i]
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

        if (index != -1)
            peakTimes[index] = (peakTimes[index] + time) / 2
        else if (peakTimes.all { abs(it - time) > MAX_PEAK_DISTANCE })
            peakTimes.add(time)
    }

    private fun List<Double>.getAveragePeakDistance(): Double {
        val iterator = iterator()
        if (!iterator.hasNext()) throw NoSuchElementException()
        val deviations = mutableListOf<Double>()

        var current = iterator.next()
        while (iterator.hasNext()) {
            val next = iterator.next()
            deviations += next - current
            current = next
        }

        return deviations.average()
    }
}
