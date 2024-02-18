package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.BpmAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.bpmanalyzing.data.SeparatedSignals
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.CombFilter
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.DifferentialRectifier
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.Filterbank
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.LowPassFilter
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FFTData
import cc.suffro.bpmanalyzer.fft.data.TimeDomainWindow
import cc.suffro.bpmanalyzer.fft.data.WindowFunction
import cc.suffro.bpmanalyzer.getHighestPowerOfTwo
import cc.suffro.bpmanalyzer.wav.data.FmtChunk
import cc.suffro.bpmanalyzer.wav.data.Wav
import java.nio.file.Path

class CombFilterAnalyzer(private val fftProcessor: FFTProcessor = FFTProcessor()) : BpmAnalyzer {
    private val cache = mutableMapOf<Path, FFTData>()

    override fun analyze(
        wav: Wav,
        start: Double,
        windowFunction: WindowFunction?,
    ): Bpm {
        val fftResult = calculateFftResult(wav, start, windowFunction)
        return fftResult
            .getBassBand(fftResult.duration)
            .getBpm(LowPassFilter(fftProcessor), CombFilter(fftProcessor), wav.fmtChunk)
    }

    private fun calculateFftResult(
        wav: Wav,
        start: Double = 0.0,
        windowFunction: WindowFunction? = null,
    ): FFTData {
        require(start + ANALYZING_DURATION < wav.trackLength) {
            "Starting time of $start seconds is too close to track end."
        }
        return cache.getOrPut(wav.filePath) {
            val window = wav.getWindow(start = start, numSamples = MINIMUM_FFT_SIZE_BY_ENERGY_LEVELS)
            fftProcessor.process(window, wav.sampleRate, windowFunction = windowFunction)
        }
    }

    private fun FFTData.getBassBand(interval: Double): TimeDomainWindow =
        Filterbank
            .separateSignals(this, MAXIMUM_FREQUENCY)
            .transformToTimeDomain(interval)
            .first()

    private fun TimeDomainWindow.getBpm(
        lowPassFilter: LowPassFilter,
        combFilter: CombFilter,
        fmtChunk: FmtChunk,
    ): Bpm {
        val lowPassFiltered = lowPassFilter.process(this, fmtChunk)
        val differentials = DifferentialRectifier.process(lowPassFiltered)

        return combFilter.process(differentials, fmtChunk.sampleRate)
    }

    private fun SeparatedSignals.transformToTimeDomain(interval: Double): Sequence<TimeDomainWindow> {
        // TODO: add better handling for low frequencies, don't cut information
        val signalInTimeDomain =
            fftProcessor.processInverse(
                values.asSequence().map {
                    val powerOfTwo = getHighestPowerOfTwo(it.size)
                    it.asSequence().take(powerOfTwo)
                },
            )

        return signalInTimeDomain.mapIndexed { index, samples -> TimeDomainWindow(samples, interval, index * interval) }
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
