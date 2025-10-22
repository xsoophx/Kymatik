package cc.suffro.bpmanalyzer.csv

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition.StartingPosition
import cc.suffro.bpmanalyzer.wav.WAVReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.nio.file.Path

fun main() {
    val samplePath = "./src/test/resources/samples/120bpm_140Hz.wav"
    val csvPath = "./src/main/kotlin/cc/suffro/bpmanalyzer/csv/resources/starting_position_output.csv"

    CSVCreator(samplePath, csvPath).generate()
}

class CSVCreator(private val sourcePath: String, private val csvPath: String) : KoinComponent {
    private val startingPositionAnalyzer by inject<CacheAnalyzer<Wav, StartingPosition>>()

    private fun writeFirstSamples(
        count: Int,
        sampleRate: Int,
        csvPathOverride: String? = null,
        sampleProvider: (Int) -> Double,
    ) {
        val outFile = File(csvPathOverride ?: csvPath)
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

    fun generate() {
        val wav = WAVReader.read(Path.of(sourcePath))

        writeFirstSamples(1000, sampleRate = wav.sampleRate) { idx ->
            wav.dataChunk.data.first()[idx]
        }
    }
}
