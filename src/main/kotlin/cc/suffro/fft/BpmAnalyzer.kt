package cc.suffro.fft

import cc.suffro.fft.data.FFTData
import cc.suffro.fft.data.Wav
import java.lang.StrictMath.abs
import java.lang.StrictMath.min
import java.math.RoundingMode
import java.nio.file.Path
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.NoSuchElementException

object BpmAnalyzer {
    private const val LOWER_FREQUENCY_BOUND = 40.0
    private const val HIGHER_FREQUENCY_BOUND = 120.0
    private const val MAX_PEAK_DISTANCE = 60.0 / 220.0

    private val fftProcessor = FFTProcessor()
    private val cache = mutableMapOf<Path, Wav>()

    fun analyze(path: Path, start: Double = 0.0, end: Double = 10.0, interval: Double = 0.01): Double {
        val wav = WAVReader.read(path)
        //cache[path] = wav
        // TODO: add nicer handling for maximum track length
        val windows = wav.getWindows(start = start, end = min(wav.trackLength - 0.1, end), interval = 0.01)

        val averagePeakTimes = fftProcessor
            .process(windows, samplingRate = wav.sampleRate)
            .getBassFrequencyBins(interval)
            .getIntervalsOverTime()
            .getAveragePeakTimes()

        val averagePeakDistance = averagePeakTimes.map { it.sorted().getAveragePeakDistance() }.average()
        return (60 / averagePeakDistance).round()
    }

    private fun Sequence<FFTData>.getBassFrequencyBins(interval: Double): Sequence<Peak> {
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

    private data class Interval(
        val midPoint: Double,
        val magnitude: Double
    )
}
