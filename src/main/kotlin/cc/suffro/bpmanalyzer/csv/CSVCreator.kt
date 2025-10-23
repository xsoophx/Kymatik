package cc.suffro.bpmanalyzer.csv

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition.StartingPosition
import cc.suffro.bpmanalyzer.wav.WAVReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.nio.file.Path

fun main(args: Array<String>) {
    CSVCreator().generate(args)
}

class CSVCreator() : KoinComponent {
    private val startingPositionAnalyzer by inject<CacheAnalyzer<Wav, StartingPosition>>()

    fun generate(args: Array<String>) {
        val parser = ArgParser("CSVCreator")

        val userSamplePath by parser.option(ArgType.String, shortName = "sp", description = "Path/name of the track")
        val userCsvPath by parser.option(ArgType.String, shortName = "csv", description = "Path to the output CSV file")
        val userSampleSize by parser.option(ArgType.Int, shortName = "size", description = "Samplesize of the track")

        parser.parse(args)

        val samplePath = userSamplePath ?: DEFAULT_PATH
        val csvPath = userCsvPath ?: DEFAULT_CSV_PATH
        val sampleSize = userSampleSize ?: DEFAULT_SAMPLE_SIZE

        val wav = WAVReader.read(Path.of(samplePath))

        writeFirstSamples(sampleSize, wav.sampleRate, csvPath) { idx ->
            wav.dataChunk.data.first()[idx]
        }
    }

    private fun writeFirstSamples(
        count: Int,
        sampleRate: Int,
        csvPath: String,
        sampleProvider: (Int) -> Double,
    ) {
        val outFile = File(csvPath)
        outFile.parentFile?.let { if (!it.exists()) it.mkdirs() }

        val header = "index,value,timeSec"

        val body =
            (0 until count)
                .asSequence()
                .map { i ->
                    val v = sampleProvider(i)
                    "$i,$v,${i.toDouble() / sampleRate}"
                }.joinToString("\n")

        outFile.writeText(header + "\n" + body + if (body.isNotEmpty()) "\n" else "")
    }

    companion object {
        const val DEFAULT_PATH = "./src/test/resources/samples/120bpm_140Hz.wav"
        const val DEFAULT_CSV_PATH =
            "./src/main/kotlin/cc/suffro/bpmanalyzer/csv/resources/starting_position_output.csv"
        const val DEFAULT_SAMPLE_SIZE = 1000
    }
}
