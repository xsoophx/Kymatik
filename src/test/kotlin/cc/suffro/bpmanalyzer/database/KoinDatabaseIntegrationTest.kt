package cc.suffro.bpmanalyzer.database

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject

open class KoinDatabaseIntegrationTest : KoinTest {

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
