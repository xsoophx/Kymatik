package cc.suffro.bpmanalyzer.database

import cc.suffro.bpmanalyzer.BaseTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInstance
import org.koin.test.inject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class KoinDatabaseIntegrationTest : BaseTest() {
    val database by inject<DatabaseOperations>()

    @AfterEach
    fun cleanUp() {
        database.cleanUpDatabase()
    }
}
