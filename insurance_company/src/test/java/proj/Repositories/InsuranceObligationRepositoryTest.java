package proj.Repositories;

import org.junit.jupiter.api.*;
import proj.Models.insurance.*;
import proj.Models.Risk;
import proj.Models.Risk.RiskCategory;
import proj.Models.Derivative;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InsuranceObligationRepositoryTest {
    private static InsuranceObligationRepository repository;
    private static DerivativeRepository derivativeRepository;
    private static LifeInsurance testLifeInsurance;
    private static HealthInsurance testHealthInsurance;
    private static PropertyInsurance testPropertyInsurance;
    private static Derivative testDerivative;

    @BeforeAll
    static void setUpBeforeClass() throws SQLException {
        // Ініціалізація репозиторіїв
        repository = new InsuranceObligationRepository();
        derivativeRepository = new DerivativeRepository();
        
        // Створення тестового деривативу
        testDerivative = new Derivative("Test Derivative");
        testDerivative = derivativeRepository.save(testDerivative);
    }

    @AfterAll
    static void tearDownAfterClass() throws SQLException {
        // Очищення тестових даних
        if (testLifeInsurance != null && testLifeInsurance.getId() != 0) {
            repository.delete(testLifeInsurance.getId());
        }
        if (testHealthInsurance != null && testHealthInsurance.getId() != 0) {
            repository.delete(testHealthInsurance.getId());
        }
        if (testPropertyInsurance != null && testPropertyInsurance.getId() != 0) {
            repository.delete(testPropertyInsurance.getId());
        }
        if (testDerivative != null && testDerivative.getId() != 0) {
            derivativeRepository.delete(testDerivative.getId());
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        // Створення тестових зобов'язань перед кожним тестом
        testLifeInsurance = new LifeInsurance(0.3, 50000.0, 12, "John Doe", true, false);
        testHealthInsurance = new HealthInsurance(0.2, 20000.0, 6, 35, false, 100000, true, true);
        testPropertyInsurance = new PropertyInsurance(0.4, 300000.0, 24, 
            "Kyiv", 500000.0, false, "APARTMENT", true);
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Очищення тестових даних після кожного тесту
        if (testLifeInsurance.getId() != 0) {
            repository.delete(testLifeInsurance.getId());
        }
        if (testHealthInsurance.getId() != 0) {
            repository.delete(testHealthInsurance.getId());
        }
        if (testPropertyInsurance.getId() != 0) {
            repository.delete(testPropertyInsurance.getId());
        }
    }

    @Test
    void testSaveAndFindById() throws SQLException {
        // Тестування LifeInsurance
        LifeInsurance savedLife = (LifeInsurance) repository.save(testLifeInsurance);
        assertTrue(savedLife.getId() > 0, "ID повинен бути більше 0 після збереження");
        
        Optional<InsuranceObligation> foundLife = repository.findById(savedLife.getId());
        assertTrue(foundLife.isPresent(), "Зобов'язання повинно бути знайдене");
        assertTrue(foundLife.get() instanceof LifeInsurance, "Тип повинен бути LifeInsurance");
        assertEquals("John Doe", ((LifeInsurance) foundLife.get()).getBeneficiary(), "Beneficiary повинен співпадати");

        // Тестування HealthInsurance
        HealthInsurance savedHealth = (HealthInsurance) repository.save(testHealthInsurance);
        Optional<InsuranceObligation> foundHealth = repository.findById(savedHealth.getId());
        assertTrue(foundHealth.isPresent(), "Зобов'язання повинно бути знайдене");
        assertEquals(35, ((HealthInsurance) foundHealth.get()).getAge(), "Age повинен співпадати");

        // Тестування PropertyInsurance
        PropertyInsurance savedProperty = (PropertyInsurance) repository.save(testPropertyInsurance);
        Optional<InsuranceObligation> foundProperty = repository.findById(savedProperty.getId());
        assertTrue(foundProperty.isPresent(), "Зобов'язання повинно бути знайдене");
        assertEquals("Kyiv", ((PropertyInsurance) foundProperty.get()).getPropertyLocation(), "Location повинен співпадати");
    }

    @Test
    void testSaveWithDerivative() throws SQLException {
        // Збереження зобов'язання з прив'язкою до деривативу
        InsuranceObligation saved = repository.save(testLifeInsurance, testDerivative);
        
        // Перевірка, що зобов'язання дійсно додане до деривативу
        Optional<Derivative> foundDerivative = derivativeRepository.findById(testDerivative.getId());
        assertTrue(foundDerivative.isPresent(), "Дериватив повинен бути знайдений");
        assertFalse(foundDerivative.get().getObligations().isEmpty(), "Дериватив повинен містити зобов'язання");
        assertEquals(saved.getId(), foundDerivative.get().getObligations().get(0).getId(), "ID зобов'язань повинні співпадати");
    }

    @Test
    void testUpdate() throws SQLException {
        // Оновлення зобов'язання
        LifeInsurance toUpdate = (LifeInsurance) repository.save(testLifeInsurance);
        toUpdate.setBeneficiary("Updated Beneficiary");
        toUpdate.setIncludesCriticalIllness(false);
        
        LifeInsurance updated = (LifeInsurance) repository.save(toUpdate);
        
        Optional<InsuranceObligation> found = repository.findById(updated.getId());
        assertTrue(found.isPresent(), "Зобов'язання повинно бути знайдене після оновлення");
        assertEquals("Updated Beneficiary", ((LifeInsurance) found.get()).getBeneficiary(), "Beneficiary повинен бути оновлений");
        assertFalse(((LifeInsurance) found.get()).includesCriticalIllness(), "IncludesCriticalIllness повинен бути оновлений");
    }

    @Test
    void testFindAll() throws SQLException {
        // Отримання всіх зобов'язань
        repository.save(testLifeInsurance);
        repository.save(testHealthInsurance);
        
        List<InsuranceObligation> obligations = repository.findAll();
        assertFalse(obligations.isEmpty(), "Список зобов'язань не повинен бути порожнім");
        assertTrue(obligations.size() >= 2, "Повинно бути щонайменше 2 зобов'язання");
    }

    @Test
    void testDelete() throws SQLException {
        // Видалення зобов'язання
        LifeInsurance toDelete = (LifeInsurance) repository.save(testLifeInsurance);
        
        boolean deleted = repository.delete(toDelete.getId());
        assertTrue(deleted, "Видалення повинно бути успішним");
        
        Optional<InsuranceObligation> found = repository.findById(toDelete.getId());
        assertFalse(found.isPresent(), "Зобов'язання не повинно бути знайдене після видалення");
    }

    @Test
    void testFindByStatus() throws SQLException {
        // Пошук за статусом
        testLifeInsurance.setStatus(InsuranceObligation.ObligationStatus.ACTIVE);
        LifeInsurance saved = (LifeInsurance) repository.save(testLifeInsurance);

        List<InsuranceObligation> activeObligations = repository.findByStatus(InsuranceObligation.ObligationStatus.ACTIVE);
        assertFalse(activeObligations.isEmpty(), "Повинен знайти активні зобов'язання");
        // Перевіряємо, що серед знайдених є зобов'язання з потрібним id
        assertTrue(
            activeObligations.stream().anyMatch(o -> o.getId() == saved.getId()),
            "Серед знайдених має бути зобов'язання з потрібним ID"
        );
    }

    @Test
    void testSaveWithRisks() throws SQLException {
        // Тест збереження з ризиками
        Risk fireRisk = new Risk("FIRE01", "Fire", "Fire risk", 0.15, RiskCategory.PROPERTY);
        Risk theftRisk = new Risk("THFT01", "Theft", "Theft risk", 0.1, RiskCategory.PROPERTY);
        Risk natdRisk = new Risk("NATD01", "Стихія", "Ризик стихійного лиха", 0.2, Risk.RiskCategory.PROPERTY);
        testPropertyInsurance.addRisk(fireRisk);
        testPropertyInsurance.addRisk(theftRisk);
        testPropertyInsurance.addRisk(natdRisk);
        
        PropertyInsurance saved = (PropertyInsurance) repository.save(testPropertyInsurance);
        
        Optional<InsuranceObligation> found = repository.findById(saved.getId());
        assertTrue(found.isPresent(), "Зобов'язання повинно бути знайдене");
        assertEquals(3, found.get().getCoveredRisks().size(), "Повинно містити 3 ризики");
    }

    @Test
    void testUpdateRisks() throws SQLException {
        // Тест оновлення ризиків
        Risk initialRisk = new Risk("HOSP01", "Госпіталізація", "Ризик госпіталізації", 0.15, Risk.RiskCategory.HEALTH);
        testHealthInsurance.addRisk(initialRisk);
        HealthInsurance saved = (HealthInsurance) repository.save(testHealthInsurance);
        
        // Оновлюємо ризики
        saved.getCoveredRisks().clear();
        Risk newRisk = new Risk("DENT01", "Стоматологія", "Ризик стоматологічних витрат", 0.1, Risk.RiskCategory.HEALTH);
        saved.addRisk(newRisk);
        HealthInsurance updated = (HealthInsurance) repository.save(saved);
        
        Optional<InsuranceObligation> found = repository.findById(updated.getId());
        assertTrue(found.isPresent(), "Зобов'язання повинно бути знайдене");
        assertEquals(1, found.get().getCoveredRisks().size(), "Повинен містити 1 ризик після оновлення");
        assertEquals("DENT01", found.get().getCoveredRisks().iterator().next().getCode(), "Код ризику повинен співпадати");
    }

    @Test
    void testUpdateRisksAtProperty() throws SQLException {
        // Тест оновлення ризиків
        Risk fireRisk = new Risk("FIRE01", "Fire", "Fire risk", 0.15, RiskCategory.PROPERTY);
        Risk theftRisk = new Risk("THFT01", "Theft", "Theft risk", 0.1, RiskCategory.PROPERTY);
        Risk natdRisk = new Risk("NATD01", "Стихія", "Ризик стихійного лиха", 0.2, Risk.RiskCategory.PROPERTY);
        testPropertyInsurance.addRisk(fireRisk);
        testPropertyInsurance.addRisk(theftRisk);
        testPropertyInsurance.addRisk(natdRisk);
        HealthInsurance saved = (HealthInsurance) repository.save(testHealthInsurance);
        
        // Оновлюємо ризики
        saved.getCoveredRisks().clear();
        Risk newRisk = new Risk("FIRE01", "Fire", "Fire risk", 0.15, RiskCategory.PROPERTY);
        saved.addRisk(newRisk);
        HealthInsurance updated = (HealthInsurance) repository.save(saved);
        
        Optional<InsuranceObligation> found = repository.findById(updated.getId());
        assertTrue(found.isPresent(), "Зобов'язання повинно бути знайдене");
        assertEquals(1, found.get().getCoveredRisks().size(), "Повинен містити 1 ризик після оновлення");
        assertEquals("FIRE01", found.get().getCoveredRisks().iterator().next().getCode(), "Код ризику повинен співпадати");
    }

    @Test
    void testSpecificTypeDataPersistence() throws SQLException {
        // Перевірка збереження специфічних даних для кожного типу
        
        // Для LifeInsurance
        LifeInsurance life = (LifeInsurance) repository.save(testLifeInsurance);
        Optional<InsuranceObligation> foundLife = repository.findById(life.getId());
        assertTrue(foundLife.isPresent() && foundLife.get() instanceof LifeInsurance);
        assertEquals("John Doe", ((LifeInsurance) foundLife.get()).getBeneficiary());
        
        // Для HealthInsurance
        HealthInsurance health = (HealthInsurance) repository.save(testHealthInsurance);
        Optional<InsuranceObligation> foundHealth = repository.findById(health.getId());
        assertTrue(foundHealth.isPresent() && foundHealth.get() instanceof HealthInsurance);
        assertEquals(35, ((HealthInsurance) foundHealth.get()).getAge());
        
        // Для PropertyInsurance
        PropertyInsurance property = (PropertyInsurance) repository.save(testPropertyInsurance);
        Optional<InsuranceObligation> foundProperty = repository.findById(property.getId());
        assertTrue(foundProperty.isPresent() && foundProperty.get() instanceof PropertyInsurance);
        assertEquals("Kyiv", ((PropertyInsurance) foundProperty.get()).getPropertyLocation());
    }

    @Test
    void testUpdatePropertyInsuranceData() throws SQLException {
        // Створюємо PropertyInsurance та зберігаємо в базу
        PropertyInsurance property = new PropertyInsurance(
                0.5, 100000.0, 36,
                "Lviv", 200000.0, false, "HOUSE", false
        );
        PropertyInsurance saved = (PropertyInsurance) repository.save(property);

        // Оновлюємо властивості
        saved.setPropertyLocation("Odesa");
        saved.setPropertyValue(300000.0);
        saved.setHighRiskArea(true);
        saved.setPropertyType("HOUSE");
        saved.setIncludesNaturalDisasters(true);

        // Зберігаємо оновлення
        PropertyInsurance updated = (PropertyInsurance) repository.save(saved);

        // Завантажуємо з бази для перевірки
        Optional<InsuranceObligation> foundOpt = repository.findById(updated.getId());
        assertTrue(foundOpt.isPresent(), "Зобов'язання повинно бути знайдене після оновлення");
        assertTrue(foundOpt.get() instanceof PropertyInsurance, "Тип повинен бути PropertyInsurance");

        PropertyInsurance found = (PropertyInsurance) foundOpt.get();
        assertEquals("Odesa", found.getPropertyLocation(), "Локація повинна бути оновлена");
        assertEquals(300000.0, found.getPropertyValue(), 0.001, "Вартість повинна бути оновлена");
        assertTrue(found.isHighRiskArea(), "HighRiskArea повинен бути true");
        assertEquals("HOUSE", found.getPropertyType(), "Тип повинен бути оновлений");
        assertTrue(found.includesNaturalDisasters(), "includesNaturalDisasters повинен бути true");

        // Прибираємо тестові дані
        repository.delete(found.getId());
    }
}