package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.ParameterizedCacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition.StartingPosition
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition.StartingPositionCacheAnalyzerParams
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.speedadjustment.SpeedAdjuster
import cc.suffro.bpmanalyzer.wav.data.Wav
import java.nio.file.Path

class TrackMerger(
    private val cacheAnalyzer: ParameterizedCacheAnalyzer<Wav, TrackInfo>,
    private val speedAdjuster: SpeedAdjuster,
    private val startingPositionAnalyzer: ParameterizedCacheAnalyzer<Wav, StartingPosition>,
) {
    fun merge(
        trackOne: Wav,
        trackTwo: Wav,
        targetBpm: Double,
    ): Wav {
        val bpmOne = cacheAnalyzer.analyze(trackOne).bpm
        val bpmTwo = cacheAnalyzer.analyze(trackTwo).bpm

        val stretchedTrackOne = Wav(trackOne, speedAdjuster.changeTo(trackOne, bpmOne, targetBpm))
        val stretchedTrackTwo = Wav(trackTwo, speedAdjuster.changeTo(trackTwo, bpmTwo, targetBpm))

        val maxLength = maxOf(stretchedTrackOne.dataChunk.data[0].size, stretchedTrackTwo.dataChunk.data[0].size)
        val numberOfChannels = maxOf(stretchedTrackOne.dataChunk.data.size, stretchedTrackTwo.dataChunk.data.size)
        val mergedSamples = Array(numberOfChannels) { DoubleArray(maxLength) { 0.0 } }

        val startingPositionOne =
            startingPositionAnalyzer.analyze(trackOne, StartingPositionCacheAnalyzerParams(bpmOne))
        val startingPositionTwo =
            startingPositionAnalyzer.analyze(trackTwo, StartingPositionCacheAnalyzerParams(bpmTwo))

        stretchedTrackOne.stretchAndWrite(startingPositionOne, maxLength, mergedSamples)
        stretchedTrackTwo.stretchAndWrite(startingPositionTwo, maxLength, mergedSamples)

        val mergedWav =
            Wav(
                trackOne.copy(filePath = Path.of("src/test/resources/tracks/merged.wav")),
                dataChunks = mergedSamples,
            )

        closeConnections()
        return mergedWav
    }

    private fun Wav.stretchAndWrite(
        startingPosition: StartingPosition,
        maxLength: Int,
        mergedSamples: Array<DoubleArray>,
    ) {
        for (channel in dataChunk.data.indices) {
            for (i in dataChunk.data[channel].indices) {
                val position = i + startingPosition.firstSample
                if (position < maxLength) {
                    mergedSamples[channel][position] += dataChunk.data[channel][i] / 2
                }
            }
        }
    }

    private fun closeConnections() {
        cacheAnalyzer.close()
        startingPositionAnalyzer.close()
    }
}
