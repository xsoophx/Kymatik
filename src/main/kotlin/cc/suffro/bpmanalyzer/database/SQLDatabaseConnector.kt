package cc.suffro.bpmanalyzer.database

import java.sql.Connection
import java.sql.DriverManager

class SQLDatabaseConnector(private val url: String) : DatabaseConnector {
    override fun getConnection(): Connection {
        return DriverManager.getConnection(url)
    }
}
