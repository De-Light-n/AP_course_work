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
 * Enhanced database manager with connection pooling and migration support
 */
public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;
    private static HikariDataSource dataSource;

    private DatabaseManager() {
        initializeConnectionPool();
        runMigrations();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

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

    private void runMigrations() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migrations")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
        logger.info("Database migrations completed");
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }

    @FunctionalInterface
    public interface TransactionOperation {
        void execute(Connection conn) throws SQLException;
    }
}