package cc.suffro.fft.bpmanalyzing.analyzers

import cc.suffro.fft.bpmanalyzing.data.SeparatedSignals
import cc.suffro.fft.bpmanalyzing.filters.CombFilter
import cc.suffro.fft.bpmanalyzing.filters.DifferentialRectifier
import cc.suffro.fft.bpmanalyzing.filters.Filterbank
import cc.suffro.fft.bpmanalyzing.filters.LowPassFilter
import cc.suffro.fft.fft.FFTProcessor
import cc.suffro.fft.fft.data.FFTData
import cc.suffro.fft.fft.data.Window
import cc.suffro.fft.fft.data.WindowFunction
import cc.suffro.fft.getHighestPowerOfTwo
import cc.suffro.fft.wav.data.FmtChunk
import cc.suffro.fft.wav.data.Wav

class CombFilterAnalyzer(private val fftProcessor: FFTProcessor = FFTProcessor()) : BpmAnalyzer {

    fun analyze(
        wav: Wav,
        start: Double = 0.0,
        windowFunction: WindowFunction? = null
    ): Double {
        require(start + ANALYZING_DURATION < wav.trackLength) {
            "Starting time of $start seconds is too close to track end."
        }

        val window = wav.getWindow(start = start, numSamples = MINIMUM_FFT_SIZE_BY_ENERGY_LEVELS)
        val fftResult = fftProcessor.process(window, wav.sampleRate, windowFunction = windowFunction)
        val filterParams = FilterParams(wav.fmtChunk, fftResult.duration, ANALYZING_DURATION)

        return fftResult.analyzeSingleWindow(LowPassFilter(fftProcessor), CombFilter(fftProcessor), filterParams)
    }

    private fun FFTData.analyzeSingleWindow(
        lowPassFilter: LowPassFilter,
        combFilter: CombFilter,
        filterParams: FilterParams
    ): Double {
        // transforms the signal into multiple signals, split by frequency intervals
        val separatedSignals = Filterbank.separateSignals(this, MAXIMUM_FREQUENCY)
        val timeTransformed = separatedSignals.transformToTimeDomain(filterParams.interval)

        val bassBand = timeTransformed.first()

        return bassBand.applyFilters(lowPassFilter, combFilter, filterParams)
    }

    private fun Window.applyFilters(
        lowPassFilter: LowPassFilter,
        combFilter: CombFilter,
        filterParams: FilterParams
    ): Double {
        val lowPassFiltered = lowPassFilter.process(this, filterParams.fmtChunk)
        val differentials = DifferentialRectifier.process(lowPassFiltered)
        val combFiltered = combFilter.process(differentials, filterParams.fmtChunk.sampleRate)

        return combFiltered.toDouble()
    }

    private fun SeparatedSignals.transformToTimeDomain(interval: Double): Sequence<Window> {
        // TODO: add better handling for low frequencies, don't cut information
        val signalInTimeDomain =
            fftProcessor.processInverse(
                values.asSequence().map {
                    val powerOfTwo = getHighestPowerOfTwo(it.size)
                    it.asSequence().take(powerOfTwo)
                }
            )

        return signalInTimeDomain.map { Window(it, interval) }
    }

    data class FilterParams(
        val fmtChunk: FmtChunk,
        val interval: Double,
        val timeFrame: Double
    )

    companion object {
        private const val MAXIMUM_FREQUENCY = 4096

        // assuming the first kick is starting at 0.0s
        // 60 bpm minimum bpm, one interval would be 2 seconds + ~ 0.3s buffer
        // minimum FFT size is determined by sampling frequency (minBpm = 60)
        // (1.0 / minBpm * 60 * samplingRate) = samplingRate
        // CombFilter has three pulses: 1, 44100, 88200 (-1 offset)
        private const val MINIMUM_FFT_SIZE_BY_ENERGY_LEVELS = 131072
        private const val ANALYZING_DURATION = 2.2
    }
}
