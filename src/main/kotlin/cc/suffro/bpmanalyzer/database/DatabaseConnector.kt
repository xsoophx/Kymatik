package cc.suffro.bpmanalyzer.database

import java.sql.Connection

fun interface DatabaseConnector {
    fun getConnection(): Connection
}