package cc.suffro.bpmanalyzer.database

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val databaseModule = module {
    singleOf(::SQLiteDatabase)
    singleOf(::SQLDatabaseConnector) {
        bind<DatabaseConnector>()
    }
}
