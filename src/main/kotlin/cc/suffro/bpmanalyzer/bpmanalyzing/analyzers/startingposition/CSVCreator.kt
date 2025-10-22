package cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.startingposition

import cc.suffro.bpmanalyzer.bpmanalyzing.analyzers.CacheAnalyzer
import cc.suffro.bpmanalyzer.wav.WAVReader
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

fun main() {
    val samplePath = "src/test/resources/samples/120bpm_140Hz.wav"
    val csvPath = "resources/starting_position_output.csv"

    CSVCreator(samplePath, csvPath).generate()
}

class CSVCreator(private val sourcePath: String, private val csvPath: String) : KoinComponent {
    private val startingPositionAnalyzer by inject<CacheAnalyzer<Wav, StartingPosition>>()

    fun generate() {
        val wav = WAVReader.read(java.nio.file.Path.of(sourcePath))
        val result = startingPositionAnalyzer.analyze(wav)
        val csvFile = File(csvPath)
        csvFile.writeText("firstSample,startInSec\n")
        csvFile.appendText("${result.firstSample},${result.startInSec}\n")
    }
}
