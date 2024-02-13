package cc.suffro.bpmanalyzer.database

import java.sql.Connection

interface DatabaseConnector {
    fun getConnection(): Connection

    fun getDatabaseNameByUrl(): String
}
