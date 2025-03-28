package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.AnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.BpmAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.database.DatabaseOperations
import cc.suffro.bpmanalyzer.fft.data.WindowFunction
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path

class CombFilterCacheAnalyzer(
    private val analyzer: BpmAnalyzer,
    private val database: DatabaseOperations,
    private val wavReader: FileReader<Wav>,
) : CacheAnalyzer<Wav, TrackInfo> {
    override fun analyze(data: Wav): TrackInfo {
        return analyze(data, CombFilterAnalyzerParams())
    }

    override fun getPathAndAnalyze(path: String): TrackInfo {
        val wav = wavReader.read(path)
        return analyze(wav)
    }

    override fun getPathAndAnalyze(path: Path): TrackInfo {
        val wav = wavReader.read(path)
        return analyze(wav)
    }

    override fun analyze(
        data: Wav,
        params: AnalyzerParams,
    ): TrackInfo {
        val start = (params as CombFilterAnalyzerParams).start
        val windowFunction = params.windowFunction

        val trackInfoFromDb = database.getTrackInfo(data.filePath)
        return trackInfoFromDb ?: database.saveAndReturnTrack(data.filePath, data, start, windowFunction)
    }

    private fun DatabaseOperations.saveAndReturnTrack(
        trackName: Path,
        wav: Wav,
        start: Double,
        windowFunction: WindowFunction?,
    ): TrackInfo {
        return saveAndReturnTrack(trackName.toString(), wav, start, windowFunction)
    }

    private fun DatabaseOperations.saveAndReturnTrack(
        trackName: String,
        wav: Wav,
        start: Double,
        windowFunction: WindowFunction?,
    ): TrackInfo {
        val bpm = analyze(trackName, wav, start, windowFunction)
        saveTrackInfo(trackName, bpm)
        return TrackInfo(trackName, bpm)
    }

    private fun analyze(
        path: String,
        wav: Wav,
        start: Double,
        windowFunction: WindowFunction?,
    ): Double {
        require(path.isNotEmpty()) { "Please provide a path to your audio file." }
        require(path.endsWith(".wav")) { "Please provide a .wav file." }

        val bpm = analyzer.analyze(wav, CombFilterAnalyzerParams(start, windowFunction))
        logger.info { "BPM: $bpm" }
        return bpm
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
