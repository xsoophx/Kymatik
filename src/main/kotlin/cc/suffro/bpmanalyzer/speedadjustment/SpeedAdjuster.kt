package cc.suffro.bpmanalyzer.speedadjustment

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.BpmAnalyzer
import cc.suffro.bpmanalyzer.wav.data.Wav
import kotlin.math.min

class SpeedAdjuster(private val bpmAnalyzer: BpmAnalyzer) {

    fun changeTo(wav: Wav, targetBpm: Double): DoubleArray {
        val currentBpm = bpmAnalyzer.analyze(wav)
        val inverseStretchFactor = targetBpm / currentBpm

        val data = wav.defaultChannel()
        val newLength = ((data.size - 1) / inverseStretchFactor).toInt() + 1
        val stretchedData = DoubleArray(newLength)

        for (i in 0 until newLength) {
            // get old values for new position
            val pos = i * inverseStretchFactor

            val lowerIndex = pos.toInt()
            val upperIndex = min(lowerIndex + 1, data.size - 1)

            val interpolation = pos - lowerIndex
            stretchedData[i] =
                data[lowerIndex] + interpolation * (data[upperIndex] - data[lowerIndex])
        }

        return stretchedData
    }
}
