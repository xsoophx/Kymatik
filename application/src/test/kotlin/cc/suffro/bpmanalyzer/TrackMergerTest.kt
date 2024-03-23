package cc.suffro.bpmanalyzer

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.koin.test.inject
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class TrackMergerTest : BaseTest() {
    private val trackMerger by inject<TrackMerger>()

    private val first = tracksWithBpm.entries.first()
    private val second = tracksWithBpm.entries.last()
    private val path = trackMerger.getMergedPathByPaths(first.key, second.key)

    @AfterEach
    fun cleanUp() {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }

    @Test
    fun `should merge two tracks correctly`() {
        val targetBpm = 140.0
        assertTrue(!File(path).exists())

        trackMerger.merge(first.toPair(), second.toPair(), targetBpm)
        assertTrue(File(path).exists())
    }

    @Test
    @Disabled
    fun `should merge two tracks correctly with different target bpm`() {
        val targetBpm = 130.0
        val trackOne = "src/test/resources/tracks/Lucinee, MRD - Bang Juice (MRD Remix).wav"
        val trackTwo = "src/test/resources/samples/120bpm_140Hz.wav"

        trackMerger.merge(trackOne to 144.0, trackTwo to 120.0, targetBpm)
    }
}
