package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.fft.data.WindowFunction
import cc.suffro.bpmanalyzer.wav.data.Wav

interface CacheAnalyzer {
    fun analyze(wav: Wav, start: Double = 0.0, windowFunction: WindowFunction? = null): TrackInfo
}