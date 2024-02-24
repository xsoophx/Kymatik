package cc.suffro.bpmanalyzer

import org.junit.jupiter.api.AfterEach
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
}
