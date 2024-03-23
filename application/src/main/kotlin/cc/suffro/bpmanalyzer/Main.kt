package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.data.Arguments
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.wav.data.Wav
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import mu.KotlinLogging
import org.koin.core.component.inject
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

private val logger = KotlinLogging.logger {}

class Main : KoinApplication() {
    fun run(args: Array<String>) {
        val arguments = parseArguments(args)
        val result = getFromDbOrAnalyze(arguments)

        println(result)
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

    private fun getFromDbOrAnalyze(arguments: Arguments): TrackInfo {
        require(arguments.trackName.isNotEmpty()) { "Please provide a path to your audio file." }
        require(arguments.trackName.endsWith(".wav")) { "Please provide a .wav file." }

        val cacheAnalyzer by inject<CacheAnalyzer<Wav, TrackInfo>>(named("ProdImpl")) {
            parametersOf(
                arguments.databaseUrl,
            )
        }
        logger.info { "Using cache analyzer for searching ${arguments.trackName} with DB url ${arguments.databaseUrl}." }

        return cacheAnalyzer.getPathAndAnalyze(arguments.trackName)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Main().run(args)
        }
    }
}
