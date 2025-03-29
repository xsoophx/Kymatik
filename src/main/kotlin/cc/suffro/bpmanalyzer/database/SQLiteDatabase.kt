package cc.suffro.bpmanalyzer.database

import cc.suffro.bpmanalyzer.data.TrackInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

private val logger = KotlinLogging.logger {}

class SQLiteDatabase(private val databaseConnector: DatabaseConnector) : DatabaseOperations {
    private val tableName = databaseConnector.getDatabaseNameByUrl()

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
            getTablesAndCreateStatement()
        } catch (e: SQLException) {
            logger.error { e.message }
        }
    }

    private fun getTablesAndCreateStatement() {
        databaseConnector.getConnection().use { connection ->
            val metaData = connection.metaData
            val tables = metaData.getTables(null, null, tableName, null)
            if (!tables.next()) {
                connection.createStatement().use { statement ->
                    statement.execute(createTableSql)
                }
            }
        }
    }

    override fun saveTrackInfo(
        trackName: Path,
        bpm: Double,
    ): Int {
        logger.info { "Trying to save $bpm for $trackName to database." }
        databaseConnector.getConnection().use { connection ->
            try {
                return prepareStatement(trackName, bpm, connection)
            } catch (e: SQLException) {
                logger.error { e.message }
                return -1
            }
        }
    }

    override fun getTrackInfo(trackName: String): TrackInfo? {
        logger.info { "Searching for $trackName in database..." }
        databaseConnector.getConnection().use { connection ->
            try {
                return getResults(trackName, connection)
            } catch (e: SQLException) {
                logger.error { e.message }
                return null
            }
        }
    }

    override fun getTrackInfo(trackName: Path): TrackInfo? {
        return getTrackInfo(trackName.toString())
    }

    override fun cleanUpDatabase(): Boolean {
        val deleteSql = "DELETE FROM $tableName"

        databaseConnector.getConnection().use { connection ->
            try {
                connection.createStatement().use { statement ->
                    statement.execute(deleteSql)
                    logger.info { "Database cleanup successful." }
                    return true
                }
            } catch (e: SQLException) {
                logger.error { "Failed to clean up database: ${e.message}" }
            }
        }
        return false
    }

    private fun prepareStatement(
        trackName: Path,
        bpm: Double,
        connection: Connection,
    ): Int {
        ensureConnectionIsOpen()

        val sql = "INSERT INTO $tableName (track_name, bpm) VALUES (?, ?)"
        val status =
            connection.prepareStatement(sql).use { statement ->
                statement.setString(1, trackName.toString())
                statement.setDouble(2, bpm)
                statement.executeUpdate()
            }

        logger.info { "Saving $trackName to database successful." }
        return status
    }

    private fun ensureConnectionIsOpen() {
        if (databaseConnector.getConnection().isClosed) {
            // Log error or throw exception
            logger.error { "Database connection is closed." }
            throw SQLException("Attempted to operate on a closed database connection.")
        }
    }

    private fun getResults(
        trackName: String,
        connection: Connection,
    ): TrackInfo? {
        val sql = "SELECT bpm FROM $tableName WHERE track_name = ?"
        connection.prepareStatement(sql).use { statement ->
            statement.setString(1, trackName)
            val resultSet = statement.executeQuery()

            if (!resultSet.next()) {
                logger.info { "Getting $trackName from database failed." }
                return null
            }

            return resultSet.getResult(trackName)
        }
    }

    private fun ResultSet.getResult(trackName: String): TrackInfo {
        val bpm = getDouble("bpm")
        logger.info { "Getting $trackName from database successful." }
        return TrackInfo(Path.of(trackName), bpm)
    }
}
