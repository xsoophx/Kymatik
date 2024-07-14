package cc.suffro.bpmanalyzer.bpmanalyzing.filters

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterAnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.data.SeparatedSignals
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Signal
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FFTData
import cc.suffro.bpmanalyzer.fft.data.TimeDomainWindow
import cc.suffro.bpmanalyzer.getHighestPowerOfTwo

class CombFilterOperationsImpl(private val combFilter: CombFilter) : CombFilterOperations {
    override fun process(
        bassSignal: Signal,
        samplingRate: Int,
        params: CombFilterAnalyzerParams,
    ) = combFilter.process(bassSignal, samplingRate, params)

    override fun getFilledFilter(
        length: Int,
        bpm: Double,
        samplingRate: Int,
    ) = combFilter.getFilledFilter(length, bpm, samplingRate)

    override fun getFrequencyBands(fftData: FFTData): List<TimeDomainWindow> {
        val separatedSignals = Filterbank.separateSignals(fftData, MAXIMUM_FREQUENCY)
        return transformToTimeDomain(separatedSignals, fftData.duration).toList()
    }

    override fun getBassBand(fftData: FFTData): TimeDomainWindow = getFrequencyBands(fftData).first()

    override fun transformToTimeDomain(
        separatedSignals: SeparatedSignals,
        interval: Double,
    ): Sequence<TimeDomainWindow> {
        // TODO: add better handling for low frequencies, don't cut information
        val signalInTimeDomain =
            FFTProcessor.processInverse(
                separatedSignals.values.asSequence().map {
                    val powerOfTwo = getHighestPowerOfTwo(it.size)
                    it.asSequence().take(powerOfTwo)
                },
            )

        return signalInTimeDomain.mapIndexed { index, samples -> TimeDomainWindow(samples, interval, index * interval) }
    }

    override fun getRelevantSamples(
        bpm: Double,
        samplingRate: Int,
        signal: DoubleArray,
    ): List<Double> = combFilter.getRelevantSamples(bpm, samplingRate, signal)

    companion object {
        private const val MAXIMUM_FREQUENCY = 4096
    }
}
