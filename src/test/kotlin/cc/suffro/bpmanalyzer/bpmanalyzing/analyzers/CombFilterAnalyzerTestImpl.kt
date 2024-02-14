package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

import cc.suffro.bpmanalyzer.bpmanalyzing.data.Bpm
import cc.suffro.bpmanalyzer.fft.data.WindowFunction
import cc.suffro.bpmanalyzer.wav.data.Wav

class CombFilterAnalyzerTestImpl : BpmAnalyzer {
    override fun analyze(wav: Wav, start: Double, windowFunction: WindowFunction?): Bpm {
        return 100.0
    }
}
