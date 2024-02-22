package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.FileWriter
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.junit.jupiter.api.Disabled
import org.koin.test.inject
import kotlin.test.Test

class TrackMergerTest : BaseTest() {
    private val trackMerger by inject<TrackMerger>()

    private val wavReader by inject<FileReader<Wav>>()

    private val wavWriter by inject<FileWriter<Wav>>()

    @Test
    @Disabled
    fun `should merge two tracks correctly`() {
        val trackOne = wavReader.read(tracksWithBpm.keys.first())
        val trackTwo = wavReader.read(tracksWithBpm.keys.last())
        val result = trackMerger.merge(trackOne, trackTwo, 130.0)

        // enable for listening to the result
        // wavWriter.write("src/test/resources/copies/merged.wav", result)
    }
}
