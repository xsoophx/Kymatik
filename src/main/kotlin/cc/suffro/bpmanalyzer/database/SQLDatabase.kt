package cc.suffro.bpmanalyzer.database

import cc.suffro.bpmanalyzer.data.TrackInfo
import mu.KotlinLogging
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

private val logger = KotlinLogging.logger {}

class SQLiteDatabase(private val url: String) : DatabaseOperations {

    private val createTableSql = """
    CREATE TABLE $TABLE_NAME (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        track_name TEXT NOT NULL,
        bpm DOUBLE PRECISION NOT NULL
    );"""

    init {
        initializeDatabase()
    }

    private fun initializeDatabase() {
        try {
            DriverManager.getConnection(url).use { connection ->
                getTablesAndCreateStatement(connection)
            }
        } catch (e: SQLException) {
            logger.error(e.message)
        }
    }

    private fun getTablesAndCreateStatement(connection: Connection) {
        val metaData = connection.metaData
        val tables = metaData.getTables(null, null, TABLE_NAME, null)

        if (!tables.next()) {
            connection.createStatement().use { statement ->
                statement.execute(createTableSql)
            }
        }
    }

    override fun saveTrackInfo(trackName: String, bpm: Double) {
        logger.info("Trying to save $bpm for $trackName to database.")
        try {
            DriverManager.getConnection(url).use { conn ->
                prepareStatement(conn, trackName, bpm)
            }
        } catch (e: SQLException) {
            logger.error(e.message)
        }
    }

    private fun prepareStatement(conn: Connection, trackName: String, bpm: Double): Int {
        val sql = "INSERT INTO $TABLE_NAME (track_name, bpm) VALUES (?, ?)"
        val status = conn.prepareStatement(sql).use { statement ->
            statement.setString(1, trackName)
            statement.setDouble(2, bpm)
            statement.executeUpdate()
        }

        logger.info("Saving $trackName to database successful.")
        return status
    }

    override fun getTrackInfo(trackName: String): TrackInfo {
        logger.info("Trying to get $trackName from database.")
        return try {
            DriverManager.getConnection(url).use { conn ->
                val sql = "SELECT bpm FROM bpm_results WHERE track_name = ?"
                conn.prepareStatement(sql).use { statement ->
                    statement.setString(1, trackName)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val bpm = resultSet.getDouble("bpm")
                        logger.info("Getting $trackName from database successful.")
                        TrackInfo(trackName, bpm)
                    } else {
                        logger.info("Getting $trackName from database failed.")
                        TrackInfo(trackName, -1.0)
                    }
                }
            }
        } catch (e: SQLException) {
            logger.error(e.message)
            return TrackInfo(trackName, -1.0)
        }
    }

    companion object {
        private const val TABLE_NAME = "bpm_results"
    }
}
