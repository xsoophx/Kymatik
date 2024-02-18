package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.combfilter

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.BpmAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzerParams
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.ParameterizedCacheAnalyzer
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.database.DatabaseOperations
import cc.suffro.bpmanalyzer.fft.data.WindowFunction
import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import mu.KotlinLogging
import java.nio.file.Path

class CombFilterCacheAnalyzer(
    private val analyzer: BpmAnalyzer,
    private val database: DatabaseOperations,
    private val wavReader: FileReader<Wav>,
) : ParameterizedCacheAnalyzer<Wav, TrackInfo> {
    override fun analyze(data: Wav): TrackInfo {
        return analyze(data, CombFilterCacheAnalyzerParams(0.0, null))
    }

    override fun getAndAnalyze(path: String): TrackInfo {
        val wav = wavReader.read(path)
        return analyze(wav)
    }

    override fun getAndAnalyze(path: Path): TrackInfo {
        val wav = wavReader.read(path)
        return analyze(wav)
    }

    override fun close() {
        database.closeConnection()
    }

    override fun analyze(
        data: Wav,
        params: CacheAnalyzerParams<TrackInfo>,
    ): TrackInfo {
        val start = (params as CombFilterCacheAnalyzerParams).start
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

        val bpm = analyzer.analyze(wav, start, windowFunction)
        logger.info { "BPM: $bpm" }
        return bpm
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
