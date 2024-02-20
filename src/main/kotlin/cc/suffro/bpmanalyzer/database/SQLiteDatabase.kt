package cc.suffro.bpmanalyzer.database

import cc.suffro.bpmanalyzer.data.TrackInfo
import mu.KotlinLogging
import java.nio.file.Path
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

private val logger = KotlinLogging.logger {}

class SQLiteDatabase(databaseConnector: DatabaseConnector) : DatabaseOperations {
    private val tableName = databaseConnector.getDatabaseNameByUrl()
    private val connection = databaseConnector.getConnection()

    private val createTableSql = """
    CREATE TABLE $tableName (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        track_name TEXT NOT NULL,
        bpm DOUBLE PRECISION NOT NULL
    );"""

    init {
        initializeDatabase()
    }

    private fun initializeDatabase() {
        try {
            getTablesAndCreateStatement(connection)
        } catch (e: SQLException) {
            logger.error(e.message)
        }
    }

    private fun getTablesAndCreateStatement(connection: Connection) {
        val metaData = connection.metaData
        val tables = metaData.getTables(null, null, tableName, null)

        if (!tables.next()) {
            connection.createStatement().use { statement ->
                statement.execute(createTableSql)
            }
        }
    }

    override fun saveTrackInfo(
        trackName: String,
        bpm: Double,
    ): Int {
        logger.info("Trying to save $bpm for $trackName to database.")
        try {
            return prepareStatement(trackName, bpm)
        } catch (e: SQLException) {
            logger.error(e.message)
            return -1
        }
    }

    override fun getTrackInfo(trackName: String): TrackInfo? {
        logger.info("Searching for $trackName in database...")
        return try {
            getResults(trackName)
        } catch (e: SQLException) {
            logger.error(e.message)
            return null
        }
    }

    override fun getTrackInfo(trackName: Path): TrackInfo? {
        return getTrackInfo(trackName.toString())
    }

    override fun cleanUpDatabase(closeConnection: Boolean): Boolean {
        val deleteSql = "DELETE FROM $tableName"

        val status =
            connection.createStatement().use { statement ->
                statement.execute(deleteSql)
            }

        when {
            status -> logger.info("Database with name $tableName: cleanup successful.")
            else -> logger.error("Database with name $tableName: cleanup failed.")
        }

        if (closeConnection) closeConnection()
        return status
    }

    override fun closeConnection() {
        return connection.close()
    }

    private fun prepareStatement(
        trackName: String,
        bpm: Double,
    ): Int {
        ensureConnectionIsOpen()

        val sql = "INSERT INTO $tableName (track_name, bpm) VALUES (?, ?)"
        val status =
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, trackName)
                statement.setDouble(2, bpm)
                statement.executeUpdate()
            }

        logger.info("Saving $trackName to database successful.")
        return status
    }

    private fun ensureConnectionIsOpen() {
        if (connection.isClosed) {
            // Log error or throw exception
            logger.error("Database connection is closed.")
            throw SQLException("Attempted to operate on a closed database connection.")
        }
    }

    private fun getResults(trackName: String): TrackInfo? {
        val sql = "SELECT bpm FROM $tableName WHERE track_name = ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, trackName)
            val resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                logger.info("Getting $trackName from database failed.")
                return null
            }

            return resultSet.getResult(trackName)
        }
    }

    private fun ResultSet.getResult(trackName: String): TrackInfo {
        val bpm = getDouble("bpm")
        logger.info("Getting $trackName from database successful.")
        return TrackInfo(trackName, bpm)
    }
}
