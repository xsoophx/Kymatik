package cc.suffro.bpmanalyzer.speedadjustment

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.wav.data.Wav
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.math.min

class SpeedAdjuster(private val cacheAnalyzer: CacheAnalyzer<Wav, TrackInfo>) {
    fun changeTo(
        wav: Wav,
        targetBpm: Double,
    ): Array<DoubleArray> {
        val trackInfo = cacheAnalyzer.analyze(wav)
        val inverseStretchFactor = targetBpm / trackInfo.bpm

        return wav.dataChunk.data.map { interpolate(it, inverseStretchFactor) }.toTypedArray()
    }

    fun changeTo(
        wav: Wav,
        currentBpm: Double,
        targetBpm: Double,
    ): Array<DoubleArray> {
        val inverseStretchFactor = targetBpm / currentBpm
        return wav.dataChunk.data.map { interpolate(it, inverseStretchFactor) }.toTypedArray()
    }

    private fun interpolate(
        data: DoubleArray,
        inverseStretchFactor: Double,
    ): DoubleArray {
        val newLength = (data.size / inverseStretchFactor).toInt()
        logger.info { "New length: $newLength" }
        val stretchedData = DoubleArray(newLength)
        logger.info { "Array created successfully" }

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

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
