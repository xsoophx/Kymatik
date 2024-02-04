package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CombFilterAnalyzer
import cc.suffro.bpmanalyzer.data.Arguments
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.database.SQLiteDatabase
import cc.suffro.bpmanalyzer.wav.WAVReader
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import mu.KotlinLogging
import org.koin.core.component.inject
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf

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
        val database by inject<SQLiteDatabase> { parametersOf(arguments.databaseUrl) }

        return (database.getTrackInfo(arguments.trackName)).let {
            if (it.bpm == -1.0) database.saveAndReturnTrack(arguments.trackName) else it
        }
    }

    private fun SQLiteDatabase.saveAndReturnTrack(trackName: String): TrackInfo {
        val bpm = analyze(trackName)
        saveTrackInfo(trackName, bpm)
        return TrackInfo(trackName, bpm)
    }

    private fun analyze(path: String): Double {
        require(path.isNotEmpty()) { "Please provide a path to your audio file." }
        require(path.endsWith(".wav")) { "Please provide a .wav file." }

        val wav = WAVReader.read(path)
        val bpm = CombFilterAnalyzer().analyze(wav)
        logger.info { "BPM: $bpm" }
        return bpm
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Main().run(args)
        }
    }
}
