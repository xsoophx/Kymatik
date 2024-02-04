package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CombFilterAnalyzer
import cc.suffro.bpmanalyzer.data.TrackInfo
import cc.suffro.bpmanalyzer.database.SQLDatabaseConnector
import cc.suffro.bpmanalyzer.database.SQLiteDatabase
import cc.suffro.bpmanalyzer.wav.WAVReader
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val parser = ArgParser("BPMAnalyzer")

            val trackName by parser.option(ArgType.String, shortName = "t", description = "Path/name of the track")
            val databaseUrl by parser.option(ArgType.String, shortName = "db", description = "Path to the database")
            val checkedDatabaseURL = databaseUrl ?: System.getenv("DATABASE_URL")

            parser.parse(args)
            requireNotNull(trackName) { "Please provide a track name." }
            requireNotNull(checkedDatabaseURL) { "Please provide a database url." }

            val databaseConnector = SQLDatabaseConnector(checkedDatabaseURL)
            val database = SQLiteDatabase(databaseConnector)

            val trackFromDatabase = (database.getTrackInfo(trackName!!)).let {
                if (it.bpm == -1.0) database.saveAndReturnTrack(trackName!!) else it
            }

            println(trackFromDatabase)
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
    }
}
