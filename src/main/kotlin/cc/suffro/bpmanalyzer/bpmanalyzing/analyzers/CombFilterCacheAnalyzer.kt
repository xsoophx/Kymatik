package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers

import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.database.DatabaseOperations
import cc.suffro.bpmanalyzer.fft.data.WindowFunction
import cc.suffro.bpmanalyzer.wav.data.Wav
import mu.KotlinLogging
import java.nio.file.Path

class CombFilterCacheAnalyzer(
    private val analyzer: BpmAnalyzer,
    private val database: DatabaseOperations
) : CacheAnalyzer {

    override fun analyze(wav: Wav, start: Double, windowFunction: WindowFunction?): TrackInfo {
        val trackInfo = (database.getTrackInfo(wav.filePath)).let {
            if (it.bpm == -1.0) database.saveAndReturnTrack(wav.filePath, wav, start, windowFunction) else it
        }
        database.closeConnection()
        return trackInfo
    }

    private fun DatabaseOperations.saveAndReturnTrack(
        trackName: Path,
        wav: Wav,
        start: Double,
        windowFunction: WindowFunction?
    ): TrackInfo {
        return saveAndReturnTrack(trackName.toString(), wav, start, windowFunction)
    }

    private fun DatabaseOperations.saveAndReturnTrack(
        trackName: String,
        wav: Wav,
        start: Double,
        windowFunction: WindowFunction?
    ): TrackInfo {
        val bpm = analyze(trackName, wav, start, windowFunction)
        saveTrackInfo(trackName, bpm)
        return TrackInfo(trackName, bpm)
    }

    private fun analyze(path: String, wav: Wav, start: Double, windowFunction: WindowFunction?): Double {
        require(path.isNotEmpty()) { "Please provide a path to your audio file." }
        require(path.endsWith(".wav")) { "Please provide a .wav file." }

        val bpm = analyzer.analyze(wav, start, windowFunction)
        logger.info { "BPM: $bpm" }
        return bpm
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
