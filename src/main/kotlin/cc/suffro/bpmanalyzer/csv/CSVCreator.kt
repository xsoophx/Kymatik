package cc.suffro.bpmanalyzer.csv

import cc.suffro.bpmanalyzer.KoinManager
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
    init {
        KoinManager.INSTANCE
    }

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
        val resultSamples =
            startingPositionAnalyzer.getTransformedSamples(wav.dataChunk.data.first().toList(), sampleSize, wav, 0)

        writeFirstSamples(resultSamples, sampleSize, wav.sampleRate, csvPath)
    }

    private fun writeFirstSamples(
        samples: List<Double>,
        count: Int,
        sampleRate: Int,
        csvPath: String,
    ) {
        val outFile = File(csvPath)
        outFile.parentFile?.let { if (!it.exists()) it.mkdirs() }

        val header = "index,value,timeSec"

        outFile.bufferedWriter().use { writer ->
            writer.write(header)
            writer.newLine()

            for (i in 0 until count) {
                val v = samples[i]
                writer.write("$i,$v,${i.toDouble() / sampleRate}")
                writer.newLine()

                if (i and 0x3FF == 0) {
                    writer.flush()
                }
            }

            writer.flush()
        }
    }

    companion object {
        const val DEFAULT_PATH = "./src/test/resources/samples/120bpm_140Hz.wav"
        const val DEFAULT_CSV_PATH =
            "./src/main/kotlin/cc/suffro/bpmanalyzer/csv/resources/starting_position_output.csv"
        const val DEFAULT_SAMPLE_SIZE = 2048
    }
}
