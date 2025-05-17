package proj.Database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;

import java.sql.*;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

/**
 * DatabaseManager is a singleton class responsible for managing the database
 * connection pool
 * and running database migrations. It uses HikariCP for connection pooling and
 * Flyway for migrations.
 * <p>
 * Usage example:
 * 
 * <pre>
 * DatabaseManager dbManager = DatabaseManager.getInstance();
 * try (Connection conn = dbManager.getConnection()) {
 *     // use connection
 * }
 * </pre>
 * </p>
 * <p>
 * Configuration is loaded from the db.properties file in the classpath.
 * </p>
 */
public class DatabaseManager {
    /** Logger for this class. */
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);

    /** Singleton instance of DatabaseManager. */
    private static DatabaseManager instance;

    /** HikariCP data source for connection pooling. */
    private static HikariDataSource dataSource;

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the connection pool and runs migrations.
     */
    private DatabaseManager() {
        initializeConnectionPool();
        runMigrations();
    }

    /**
     * Returns the singleton instance of DatabaseManager.
     *
     * @return the singleton instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Initializes the HikariCP connection pool using properties from db.properties.
     *
     * @throws IllegalStateException if the configuration file is not found or
     *                               cannot be loaded
     */
    private void initializeConnectionPool() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream == null) {
                throw new IllegalStateException("Файл конфігурації не знайдено: db.properties");
            }

            Properties props = new Properties();
            props.load(inputStream);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.password"));
            config.setDriverClassName(props.getProperty("db.driver", "org.postgresql.Driver"));

            // Pool configuration
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.size", "10")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.connection.timeout", "30000")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.idle.timeout", "600000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.max.lifetime", "1800000")));
            config.setLeakDetectionThreshold(Long.parseLong(props.getProperty("db.leak.threshold", "30000")));

            dataSource = new HikariDataSource(config);
            logger.info("Database connection pool initialized successfully");

        } catch (IOException e) {
            logger.fatal("Error loading database configuration: {}", e.getMessage());
            throw new IllegalStateException("Error loading database configuration", e);
        }
    }

    /**
     * Runs Flyway database migrations.
     */
    private void runMigrations() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migrations")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
        logger.info("Database migrations completed");
    }

    /**
     * Returns a connection from the connection pool.
     *
     * @return a {@link Connection} object
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Closes the connection pool and releases all resources.
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }
}