package cc.suffro.bpmanalyzer.wav.data

import cc.suffro.bpmanalyzer.fft.data.FftSampleSize

data class WindowProcessingParams(
    val start: Double = 0.0,
    val end: Double,
    val interval: Double,
    val channel: Int = 0,
    val numSamples: Int = FftSampleSize.DEFAULT
)
