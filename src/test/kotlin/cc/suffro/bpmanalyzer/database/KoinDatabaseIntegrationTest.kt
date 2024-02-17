package cc.suffro.bpmanalyzer.database

import cc.suffro.bpmanalyzer.BaseTest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.test.inject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class KoinDatabaseIntegrationTest : BaseTest() {

    val database by inject<DatabaseOperations>()

    @AfterEach
    fun cleanUp() {
        database.cleanUpDatabase()
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp() {
            startKoin {
                modules(databaseTestModule)
            }
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            stopKoin()
        }
    }
}
