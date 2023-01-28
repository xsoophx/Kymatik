package cc.suffro.fft.bpmanalyzing.analyzers

import cc.suffro.fft.bpmanalyzing.data.Bpm
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
    ): Bpm {
        require(start + ANALYZING_DURATION < wav.trackLength) {
            "Starting time of $start seconds is too close to track end."
        }
        val window = wav.getWindow(start = start, numSamples = MINIMUM_FFT_SIZE_BY_ENERGY_LEVELS)
        val fftResult = fftProcessor.process(window, wav.sampleRate, windowFunction = windowFunction)

        return fftResult
            .getBassBand(fftResult.duration)
            .getBpm(LowPassFilter(fftProcessor), CombFilter(fftProcessor), wav.fmtChunk)
    }

    private fun FFTData.getBassBand(interval: Double): Window =
        Filterbank
            .separateSignals(this, MAXIMUM_FREQUENCY)
            .transformToTimeDomain(interval)
            .first()

    private fun Window.getBpm(
        lowPassFilter: LowPassFilter,
        combFilter: CombFilter,
        fmtChunk: FmtChunk
    ): Bpm {
        val lowPassFiltered = lowPassFilter.process(this, fmtChunk)
        val differentials = DifferentialRectifier.process(lowPassFiltered)

        return combFilter.process(differentials, fmtChunk.sampleRate)
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
