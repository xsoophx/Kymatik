package cc.suffro.bpmanalyzer.database

import org.junit.jupiter.api.Test
import java.nio.file.Path

class SQLiteDatabaseTest : KoinDatabaseIntegrationTest() {
    @Test
    fun `should create a table`() {
        val result = database.saveTrackInfo(Path.of("test.wav"), 155.5)
        assert(result == 1)
    }
}
