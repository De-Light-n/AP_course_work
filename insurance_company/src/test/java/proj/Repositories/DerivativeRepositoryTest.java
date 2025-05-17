package proj.Repositories;

import org.junit.jupiter.api.*;
import proj.Models.Derivative;
import proj.Models.insurance.InsuranceObligation;
import proj.Models.insurance.HealthInsurance;
import proj.Models.insurance.LifeInsurance;
import proj.Models.insurance.PropertyInsurance;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DerivativeRepositoryTest {
    private static DerivativeRepository repository;
    private static InsuranceObligationRepository obligationRepository;
    private static Derivative testDerivative;
    private static InsuranceObligation testObligation1;
    private static InsuranceObligation testObligation2;

    @BeforeAll
    static void setUpBeforeClass() throws SQLException {
        // Ініціалізація репозиторіїв
        repository = new DerivativeRepository();
        obligationRepository = new InsuranceObligationRepository();
        
        // Створення тестових об'єктів
        testObligation1 = new HealthInsurance(0.5, 1000.0, 12, 35, false, 50000, true, false);
        testObligation1 = obligationRepository.save(testObligation1);
        
        testObligation2 = new LifeInsurance(0.3, 2000.0, 24, "John Doe", true, false);
        testObligation2 = obligationRepository.save(testObligation2);
        
        testDerivative = new Derivative("Test Derivative");
        testDerivative.addObligation(testObligation1);
        testDerivative.addObligation(testObligation2);
    }

    @AfterAll
    static void tearDownAfterClass() throws SQLException {
        // Очищення тестових даних
        if (testDerivative.getId() != 0) {
            repository.delete(testDerivative.getId());
        }
        obligationRepository.delete(testObligation1.getId());
        obligationRepository.delete(testObligation2.getId());
    }

    @Test
    void testSaveAndFindById() throws SQLException {
        // Збереження деривативу
        Derivative saved = repository.save(testDerivative);
        assertTrue(saved.getId() > 0, "ID повинен бути більше 0 після збереження");
        
        // Пошук за ID
        Optional<Derivative> found = repository.findById(saved.getId());
        assertTrue(found.isPresent(), "Дериватив повинен бути знайдений");
        
        Derivative derivative = found.get();
        assertEquals(saved.getId(), derivative.getId(), "ID повинні співпадати");
        assertEquals("Updated Test Derivative", derivative.getName(), "Назви повинні співпадати");
        assertTrue(derivative.getTotalValue() > 0, "Загальна вартість повинна бути більше 0");
    }

    @Test
    void testUpdate() throws SQLException {
        // Оновлення деривативу
        Derivative toUpdate = repository.save(testDerivative);
        toUpdate.setName("Updated Test Derivative");
        toUpdate.removeObligation(testObligation2);
        
        Derivative updated = repository.save(toUpdate);
        
        Optional<Derivative> found = repository.findById(updated.getId());
        assertTrue(found.isPresent(), "Дериватив повинен бути знайдений після оновлення");
        
        Derivative derivative = found.get();
        assertEquals("Updated Test Derivative", derivative.getName(), "Назва повинна бути оновлена");
        assertEquals(1, derivative.getObligations().size(), "Кількість зобов'язань повинна бути 1 після оновлення");
    }

    @Test
    void testFindAll() throws SQLException {
        // Створення додаткового деривативу для тесту
        Derivative anotherDerivative = new Derivative("Another Test Derivative");
        anotherDerivative = repository.save(anotherDerivative);
        
        List<Derivative> derivatives = repository.findAll();
        assertFalse(derivatives.isEmpty(), "Список деривативів не повинен бути порожнім");
        // Очищення тестового деривативу
        repository.delete(anotherDerivative.getId());
    }

    @Test
    void testDelete() throws SQLException {
        // Створення та видалення деривативу
        Derivative toDelete = new Derivative("To Delete");
        toDelete = repository.save(toDelete);
        
        boolean deleted = repository.delete(toDelete.getId());
        assertTrue(deleted, "Видалення повинно бути успішним");
        
        Optional<Derivative> found = repository.findById(toDelete.getId());
        assertFalse(found.isPresent(), "Дериватив не повинен бути знайдений після видалення");
    }

    @Test
    void testFindByName() throws SQLException {
        // Пошук за назвою
        Derivative namedDerivative = new Derivative("Specific Name Test");
        namedDerivative = repository.save(namedDerivative);
        
        List<Derivative> found = repository.findByName("Specific");
        assertFalse(found.isEmpty(), "Повинен знайти дериватив за частиною назви");
        assertEquals("Specific Name Test", found.get(0).getName(), "Назви повинні співпадати");
        
        // Очищення тестового деривативу
        repository.delete(namedDerivative.getId());
    }

    @Test
    void testFindByTotalValueRange() throws SQLException {
        // Пошук за діапазоном вартості
        Derivative valueDerivative = new Derivative("Value Test");
        valueDerivative.addObligation(testObligation1); // Вартість ~2150
        valueDerivative = repository.save(valueDerivative);
        
        List<Derivative> found = repository.findByTotalValueRange(2100, 2200);
        assertFalse(found.isEmpty(), "Повинен знайти дериватив у вказаному діапазоні вартості");
        
        // Очищення тестового деривативу
        repository.delete(valueDerivative.getId());
    }

    @Test
    void testSaveWithNewObligations() throws SQLException {
        // Тест збереження з новими зобов'язаннями
        Derivative derivative = new Derivative("New Obligations Test");
        
        // Створення нового зобов'язання без ID
        InsuranceObligation newObligation = new PropertyInsurance(0.4, 1500.0, 6, 
            "Kyiv", 200000.0, false, "APARTMENT", true);
        
        derivative.addObligation(newObligation);
        Derivative saved = repository.save(derivative);
        
        // Перевірка, що зобов'язання отримало ID
        Optional<Derivative> found = repository.findById(saved.getId());
        assertTrue(found.isPresent(), "Дериватив повинен бути знайдений");
        assertFalse(found.get().getObligations().isEmpty(), "Повинно містити зобов'язання");
        assertTrue(found.get().getObligations().get(0).getId() > 0, "Зобов'язання повинно мати ID");
        
        // Очищення тестових даних
        repository.delete(saved.getId());
        obligationRepository.delete(found.get().getObligations().get(0).getId());
    }
}