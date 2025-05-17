package proj.Database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class DatabaseManagerTest {

    private static DatabaseManager dbManager;

    @BeforeAll
    static void setUp() {
        dbManager = DatabaseManager.getInstance();
    }

    @AfterAll
    static void tearDown() {
        dbManager.close();
    }

    @Test
    void testSingletonInstance() {
        DatabaseManager anotherInstance = DatabaseManager.getInstance();
        assertSame(dbManager, anotherInstance, "Очікується той самий екземпляр DatabaseManager");
    }

    @Test
    void testConnectionPoolConfiguration() {
        // Цей тест перевіряє, що конфігурація пулу з'єднань завантажується коректно
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            assertNotNull(inputStream, "Файл db.properties не знайдено");

            Properties props = new Properties();
            props.load(inputStream);

            assertEquals("org.postgresql.Driver", props.getProperty("db.driver"), 
                "Драйвер бази даних має бути org.postgresql.Driver");
            assertNotNull(props.getProperty("db.url"), "URL бази даних не має бути null");
            assertNotNull(props.getProperty("db.user"), "Користувач бази даних не має бути null");
            assertNotNull(props.getProperty("db.password"), "Пароль бази даних не має бути null");

            int poolSize = Integer.parseInt(props.getProperty("db.pool.size", "10"));
            assertTrue(poolSize > 0, "Розмір пулу має бути більше 0");

        } catch (IOException e) {
            fail("Помилка при читанні файлу конфігурації: " + e.getMessage());
        }
    }

    @Test
    void testCloseConnectionPool() {
        DatabaseManager testManager = DatabaseManager.getInstance();
        testManager.close();
        
        assertThrows(SQLException.class, () -> {
            testManager.getConnection();
        }, "Очікується SQLException після закриття пулу з'єднань");
    }
}