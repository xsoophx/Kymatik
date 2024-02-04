package cc.suffro.bpmanalyzer.database

import cc.suffro.bpmanalyzer.data.TrackInfo
import mu.KotlinLogging
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

private val logger = KotlinLogging.logger {}

class SQLiteDatabase(private val databaseConnector: DatabaseConnector) : DatabaseOperations {

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
            databaseConnector.getConnection().use { connection ->
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
            databaseConnector.getConnection().use { conn ->
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
            databaseConnector.getConnection().use { conn ->
                getResults(conn, trackName)
            }
        } catch (e: SQLException) {
            logger.error(e.message)
            return TrackInfo(trackName, -1.0)
        }
    }

    private fun getResults(conn: Connection, trackName: String): TrackInfo {
        val sql = "SELECT bpm FROM bpm_results WHERE track_name = ?"
        conn.prepareStatement(sql).use { statement ->
            statement.setString(1, trackName)
            val resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                logger.info("Getting $trackName from database failed.")
                return TrackInfo(trackName, -1.0)
            }

            return resultSet.getResult(trackName)
        }
    }

    private fun ResultSet.getResult(trackName: String): TrackInfo {
        val bpm = getDouble("bpm")
        logger.info("Getting $trackName from database successful.")
        return TrackInfo(trackName, bpm)
    }

    companion object {
        private const val TABLE_NAME = "bpm_results"
    }
}
