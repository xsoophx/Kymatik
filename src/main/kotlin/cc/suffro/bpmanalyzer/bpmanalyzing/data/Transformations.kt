package cc.suffro.bpmanalyzer.bpmanalyzing.data

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter.CombFilterAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.Filterbank
import cc.suffro.bpmanalyzer.fft.FFTProcessor
import cc.suffro.bpmanalyzer.fft.data.FFTData
import cc.suffro.bpmanalyzer.fft.data.TimeDomainWindow
import cc.suffro.bpmanalyzer.getHighestPowerOfTwo

fun FFTData.getFrequencyBands(
    interval: Double,
    fftProcessor: FFTProcessor,
): List<TimeDomainWindow> =
    Filterbank
        .separateSignals(this, CombFilterAnalyzer.MAXIMUM_FREQUENCY)
        .transformToTimeDomain(interval, fftProcessor)
        .toList()

fun FFTData.getBassBand(
    interval: Double,
    fftProcessor: FFTProcessor,
): TimeDomainWindow = getFrequencyBands(interval, fftProcessor).first()

fun SeparatedSignals.transformToTimeDomain(
    interval: Double,
    fftProcessor: FFTProcessor,
): Sequence<TimeDomainWindow> {
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
