package cc.suffro.bpmanalyzer.database

import org.junit.jupiter.api.Test

class SQLiteDatabaseTest : KoinDatabaseIntegrationTest() {

    @Test
    fun `should create a table`() {
        val result = database.saveTrackInfo("test.wav", 155.5)
        assert(result == 1)
    }
}
