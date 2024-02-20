package cc.suffro.bpmanalyzer

import cc.suffro.bpmanalyzer.wav.data.FileReader
import cc.suffro.bpmanalyzer.wav.data.FileWriter
import cc.suffro.bpmanalyzer.wav.data.Wav
import org.koin.test.inject
import kotlin.test.Test

class TrackMergerTest : BaseTest() {
    private val trackMerger by inject<TrackMerger>()

    private val wavReader by inject<FileReader<Wav>>()

    private val wavWriter by inject<FileWriter<Wav>>()

    @Test
    fun `should merge two tracks correctly`() {
        val trackOne = wavReader.read("src/test/resources/tracks/LEE ft. Anivalence - Waves (Scove Remix).wav")
        val trackTwo = wavReader.read("src/test/resources/tracks/Mark Terre - Gravity Zero.wav")
        val result = trackMerger.merge(trackOne, trackTwo, 130.0)
        wavWriter.write("src/test/resources/tracks/merged.wav", result)
    }
}
