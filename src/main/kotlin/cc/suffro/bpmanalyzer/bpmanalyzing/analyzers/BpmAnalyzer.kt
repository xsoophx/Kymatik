package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.fft.data.WindowFunction
import cc.suffro.bpmanalyzer.wav.data.Wav

interface BpmAnalyzer {
    fun analyze(
        wav: Wav,
        start: Double = 0.0,
        windowFunction: WindowFunction? = null,
    ): Bpm
}
