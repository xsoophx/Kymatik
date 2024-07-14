package cc.suffro.bpmanalyzer.bpmanalyzing.filters

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterAnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.data.SeparatedSignals
import cc.suffro.bpmanalyzer.bpmanalyzing.data.Signal
import cc.suffro.bpmanalyzer.fft.data.FFTData
import cc.suffro.bpmanalyzer.fft.data.TimeDomainWindow

interface CombFilterOperations {
    fun process(
        bassSignal: Signal,
        samplingRate: Int,
        params: CombFilterAnalyzerParams,
    ): Double

    fun getFilledFilter(
        length: Int,
        bpm: Double,
        samplingRate: Int,
    ): List<Double>

    fun getFrequencyBands(fftData: FFTData): List<TimeDomainWindow>

    fun getBassBand(fftData: FFTData): TimeDomainWindow

    fun transformToTimeDomain(
        separatedSignals: SeparatedSignals,
        interval: Double,
    ): Sequence<TimeDomainWindow>

    fun getRelevantSamples(
        bpm: Double,
        samplingRate: Int,
        signal: DoubleArray,
    ): List<Double>
}
