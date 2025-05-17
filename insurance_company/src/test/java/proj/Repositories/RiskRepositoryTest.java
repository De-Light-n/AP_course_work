package proj.Repositories;

import org.junit.jupiter.api.*;
import proj.Models.Risk;
import proj.Models.Risk.RiskCategory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RiskRepositoryTest {
    private static RiskRepository repository;
    private static Risk testRisk;

    @BeforeAll
    static void setUpBeforeClass() throws SQLException {
        // Ініціалізація репозиторію
        repository = new RiskRepository();
        // Ініціалізація стандартних ризиків
        repository.initializeStandardRisks();
    }

    @BeforeEach
    void setUp() {
        // Створення тестового ризику перед кожним тестом
        testRisk = new Risk("TEST01", "Test Risk", "Test Description", 0.12, RiskCategory.HEALTH);
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Очищення тестового ризику після кожного тесту
        if (repository.existsByCode(testRisk.getCode())) {
            repository.delete(testRisk.getCode());
        }
    }

    @Test
    void testSaveAndFindByCode() throws SQLException {
        // Збереження ризику
        Risk savedRisk = repository.save(testRisk);
        assertEquals(testRisk.getCode(), savedRisk.getCode(), "Коди ризиків повинні співпадати");
        
        // Пошук за кодом
        Optional<Risk> foundRisk = repository.findByCode(testRisk.getCode());
        assertTrue(foundRisk.isPresent(), "Ризик повинен бути знайдений");
        assertEquals(testRisk.getName(), foundRisk.get().getName(), "Назви ризиків повинні співпадати");
    }

    @Test
    void testUpdate() throws SQLException {
        // Оновлення ризику
        Risk savedRisk = repository.save(testRisk);
        savedRisk.setName("Updated Risk Name");
        savedRisk.setBaseRiskFactor(0.15);
        
        repository.save(savedRisk);
        
        Optional<Risk> foundRisk = repository.findByCode(testRisk.getCode());
        assertTrue(foundRisk.isPresent(), "Ризик повинен бути знайдений після оновлення");
        assertEquals("Updated Risk Name", foundRisk.get().getName(), "Назва повинна бути оновлена");
        assertEquals(0.15, foundRisk.get().getBaseRiskFactor(), 0.001, "Коефіцієнт ризику повинен бути оновлений");
    }

    @Test
    void testFindAll() throws SQLException {
        // Отримання всіх ризиків
        repository.save(testRisk);
        List<Risk> risks = repository.findAll();
        
        assertFalse(risks.isEmpty(), "Список ризиків не повинен бути порожнім");
        assertTrue(risks.size() >= 1, "Повинен містити щонайменше 1 ризик");
    }

    @Test
    void testDelete() throws SQLException {
        // Видалення ризику
        repository.save(testRisk);
        boolean deleted = repository.delete(testRisk.getCode());
        
        assertTrue(deleted, "Видалення повинно бути успішним");
        assertFalse(repository.existsByCode(testRisk.getCode()), "Ризик не повинен існувати після видалення");
    }

    @Test
    void testFindByCategory() throws SQLException {
        // Пошук за категорією
        repository.save(testRisk);
        List<Risk> healthRisks = repository.findByCategory(RiskCategory.HEALTH);

        assertFalse(healthRisks.isEmpty(), "Повинен знайти ризики за категорією HEALTH");
        assertTrue(
            healthRisks.stream().anyMatch(r -> r.getCode().equals(testRisk.getCode())) ||
            healthRisks.stream().anyMatch(r -> r.getCode().equals("HLTH01")) ||
            healthRisks.stream().anyMatch(r -> r.getCode().equals("HOSP01")) ||
            healthRisks.stream().anyMatch(r -> r.getCode().equals("DENT01")),
            "Повинен містити тестовий або стандартні ризики HEALTH"
        );
    }

    @Test
    void testExistsByCode() throws SQLException {
        // Перевірка існування ризику за кодом
        repository.save(testRisk);
        boolean exists = repository.existsByCode(testRisk.getCode());
        
        assertTrue(exists, "Ризик повинен існувати");
        assertFalse(repository.existsByCode("NON_EXISTENT_CODE"), "Неіснуючий код повинен повертати false");
    }

    @Test
    void testFindByNamePattern() throws SQLException {
        // Пошук за шаблоном назви
        repository.save(testRisk);
        List<Risk> foundRisks = repository.findByNamePattern("Test");
        
        assertFalse(foundRisks.isEmpty(), "Повинен знайти ризики за шаблоном назви");
        assertTrue(foundRisks.stream().anyMatch(r -> r.getCode().equals(testRisk.getCode())),
                 "Повинен містити тестовий ризик");
    }

    @Test
    void testFindByRiskFactorRange() throws SQLException {
        // Пошук за діапазоном коефіцієнта ризику
        repository.save(testRisk);
        List<Risk> foundRisks = repository.findByRiskFactorRange(0.1, 0.15);

        assertFalse(foundRisks.isEmpty(), "Повинен знайти ризики у вказаному діапазоні");
        assertTrue(
            foundRisks.stream().anyMatch(r -> r.getCode().equals(testRisk.getCode())) ||
            foundRisks.stream().anyMatch(r -> r.getBaseRiskFactor() >= 0.1 && r.getBaseRiskFactor() <= 0.15),
            "Повинен містити тестовий або інші ризики у діапазоні"
        );
    }

    @Test
    void testInitializeStandardRisks() throws SQLException {
        // Перевірка ініціалізації стандартних ризиків
        // Видаляємо один зі стандартних ризиків для тесту
        repository.delete("HLTH01");
        
        // Ініціалізуємо стандартні ризики
        repository.initializeStandardRisks();
        
        // Перевіряємо, що стандартні ризики існують
        assertTrue(repository.existsByCode("HLTH01"), "Стандартний ризик HLTH01 повинен існувати");
        assertTrue(repository.existsByCode("DEATH01"), "Стандартний ризик DEATH01 повинен існувати");
        assertTrue(repository.existsByCode("FIRE01"), "Стандартний ризик FIRE01 повинен існувати");
    }

    @Test
    void testFindByObligationId() throws SQLException {
        assertDoesNotThrow(() -> repository.findByObligationId(1));
    }

    @Test
    void testSaveWithNullCode() {
        // Перевірка обробки null коду
        assertThrows(IllegalArgumentException.class, () -> {
            Risk nullCodeRisk = new Risk(null, "Null Code Risk", "Description", 0.1, RiskCategory.HEALTH);
            repository.save(nullCodeRisk);
        }, "Повинен кидати виняток при спробі створити ризик з null кодом");
    }
}