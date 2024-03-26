package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.bpmAnalyzingModule
import cc.suffro.bpmanalyzer.bpmanalyzing.filters.filterModule
import cc.suffro.bpmanalyzer.data.Arguments
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.database.databaseModule
import cc.suffro.bpmanalyzer.fft.fftModule
import cc.suffro.bpmanalyzer.speedadjustment.speedAdjusterModule
import cc.suffro.bpmanalyzer.wav.data.Wav
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import mu.KotlinLogging
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

open class BpmAnalyzer : BpmOperations {
    init {
        init()
    }

    final override fun init() {
        startKoin {
            modules(appModule, databaseModule, speedAdjusterModule, fftModule, bpmAnalyzingModule, filterModule)
        }
    }

    override fun analyze(args: Array<String>): TrackInfo {
        val arguments = parseArguments(args)
        val result = getFromDbOrAnalyze(arguments)

        println(result)
        return result
    }

    override fun analyze(
        trackPath: String,
        databasePath: String,
    ): TrackInfo {
        val result = getFromDbOrAnalyze(trackPath, databasePath)

        println(result)
        return result
    }

    override fun close() {
        stopKoin()
    }

    private fun parseArguments(args: Array<String>): Arguments {
        val parser by inject<ArgParser>()

        val trackName by parser.option(ArgType.String, shortName = "t", description = "Path/name of the track")
        val databaseUrl by parser.option(ArgType.String, shortName = "db", description = "Path to the database")
        val checkedDatabaseURL = databaseUrl ?: System.getenv("DATABASE_URL")

        parser.parse(args)
        requireNotNull(trackName) { "Please provide a track name." }
        requireNotNull(checkedDatabaseURL) { "Please provide a database url." }

        return Arguments(trackName!!, checkedDatabaseURL)
    }

    private fun getFromDbOrAnalyze(
        trackPath: String,
        databasePath: String,
    ): TrackInfo {
        require(trackPath.isNotEmpty()) { "Please provide a path to your audio file." }
        require(databasePath.endsWith(".wav")) { "Please provide a .wav file." }

        val cacheAnalyzer by inject<CacheAnalyzer<Wav, TrackInfo>>(named("ProdImpl")) { parametersOf(databasePath) }
        logger.info { "Using cache analyzer for searching $trackPath with DB url $databasePath." }

        return cacheAnalyzer.getPathAndAnalyze(trackPath)
    }

    private fun getFromDbOrAnalyze(arguments: Arguments): TrackInfo {
        return getFromDbOrAnalyze(arguments.trackPath, arguments.databasePath)
    }

    companion object : BpmOperations by BpmAnalyzer() {
        private val logger = KotlinLogging.logger {}
    }
}
