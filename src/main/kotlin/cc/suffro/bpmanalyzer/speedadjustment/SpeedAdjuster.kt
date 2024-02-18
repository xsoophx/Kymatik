package cc.suffro.bpmanalyzer.speedadjustment

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.wav.data.Wav
import kotlin.math.min

class SpeedAdjuster(private val cacheAnalyzer: CacheAnalyzer) {

    fun changeTo(wav: Wav, targetBpm: Double): Array<DoubleArray> {
        val trackInfo = cacheAnalyzer.analyze(wav)
        val inverseStretchFactor = targetBpm / trackInfo.bpm

        return wav.dataChunk.data.map { interpolate(it, inverseStretchFactor) }.toTypedArray()
    }

    private fun interpolate(data: DoubleArray, inverseStretchFactor: Double): DoubleArray {
        val newLength = (data.size / inverseStretchFactor).toInt()
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
