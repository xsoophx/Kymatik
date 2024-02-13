package cc.suffro.bpmanalyzer.database

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

const val DATABASE_TEST_NAME = "test"

val databaseTestModule = module {
    singleOf(::SQLiteDatabase) {
        bind<DatabaseOperations>()
    }
    single<DatabaseConnector> {
        SQLDatabaseConnector(
            System.getenv("DATABASE_TEST_URL") ?: "jdbc:sqlite:src/test/resources/$DATABASE_TEST_NAME.db"
        )
    }
    singleOf(::SQLiteDatabaseTest)
}
