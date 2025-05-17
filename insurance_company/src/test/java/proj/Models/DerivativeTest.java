package proj.Models;

import proj.Models.insurance.HealthInsurance;
import proj.Models.insurance.InsuranceObligation;
import proj.Models.insurance.LifeInsurance;
import proj.Models.insurance.PropertyInsurance;
import proj.Models.Risk.RiskCategory;

import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DerivativeTest {
    private Derivative derivative;
    private InsuranceObligation lifeInsurance;
    private InsuranceObligation healthInsurance;
    private InsuranceObligation propertyInsurance;

    @BeforeEach
    void setUp() {
        derivative = new Derivative("Test Derivative");
        
        // Створення тестових зобов'язань
        lifeInsurance = new LifeInsurance(0.5, 100000.0, 12, "John Doe", true, false);
        healthInsurance = new HealthInsurance(0.3, 50000.0, 6, 35, false, 200000, true, false);
        propertyInsurance = new PropertyInsurance(0.4, 500000.0, 24, 
            "Kyiv", 1000000.0, false, "APARTMENT", true);
        
        // Додавання стандартних ризиків
        lifeInsurance.addRisk(new Risk("DEATH01", "Death", "Risk of death", 0.25, RiskCategory.LIFE));
        healthInsurance.addRisk(new Risk("HLTH01", "Health", "Health risk", 0.2, RiskCategory.HEALTH));
        propertyInsurance.addRisk(new Risk("FIRE01", "Fire", "Fire risk", 0.15, RiskCategory.PROPERTY));
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("Test Derivative", derivative.getName());
        assertTrue(derivative.getObligations().isEmpty());
        assertEquals(0.0, derivative.getTotalValue(), 0.001);
        assertNotNull(derivative.getCreatedAt());
        assertNotNull(derivative.getUpdatedAt());
    }

    @Test
    void testSetters() {
        derivative.setId(1);
        assertEquals(1, derivative.getId());
        
        derivative.setName("Updated Name");
        assertEquals("Updated Name", derivative.getName());
        
        derivative.setTotalValue(1000.0);
        assertEquals(1000.0, derivative.getTotalValue(), 0.001);
        
        LocalDateTime now = LocalDateTime.now();
        derivative.setCreatedAt(now);
        assertEquals(now, derivative.getCreatedAt());
        
        derivative.setUpdatedAt(now);
        assertEquals(now, derivative.getUpdatedAt());
    }

    @Test
    void testAddAndRemoveObligation() {
        // Додавання зобов'язання
        derivative.addObligation(lifeInsurance);
        assertEquals(1, derivative.getObligations().size());
        assertTrue(derivative.getTotalValue() > 0);
        
        // Спроба додати те саме зобов'язання повторно
        derivative.addObligation(lifeInsurance);
        assertEquals(1, derivative.getObligations().size());
        
        // Видалення зобов'язання
        derivative.removeObligation(lifeInsurance);
        assertTrue(derivative.getObligations().isEmpty());
        assertEquals(0.0, derivative.getTotalValue(), 0.001);
    }

    @Test
    void testSetObligations() {
        List<InsuranceObligation> obligations = List.of(lifeInsurance, healthInsurance);
        derivative.setObligations(obligations);
        
        assertEquals(2, derivative.getObligations().size());
        assertTrue(derivative.getTotalValue() > 0);
    }

    @Test
    void testSortByRiskLevel() {
        derivative.addObligation(lifeInsurance); // riskLevel = 0.5
        derivative.addObligation(healthInsurance); // riskLevel = 0.3
        derivative.addObligation(propertyInsurance); // riskLevel = 0.4
        
        derivative.sortByRiskLevel();
        
        List<InsuranceObligation> sorted = derivative.getObligations();
        assertEquals(0.5, sorted.get(0).getRiskLevel(), 0.001);
        assertEquals(0.4, sorted.get(1).getRiskLevel(), 0.001);
        assertEquals(0.3, sorted.get(2).getRiskLevel(), 0.001);
    }

    @Test
    void testFindObligationsInRange() {
        derivative.addObligation(lifeInsurance); // risk=0.5, amount=100000
        derivative.addObligation(healthInsurance); // risk=0.3, amount=50000
        derivative.addObligation(propertyInsurance); // risk=0.4, amount=500000
        
        List<InsuranceObligation> result = derivative.findObligationsInRange(0.3, 0.4, 40000, 60000);
        
        assertEquals(1, result.size());
        assertEquals(healthInsurance, result.get(0));
    }

    @Test
    void testCountRisksByCategory() {
        derivative.addObligation(lifeInsurance);
        derivative.addObligation(healthInsurance);
        derivative.addObligation(propertyInsurance);
        
        Map<RiskCategory, Long> counts = derivative.countRisksByCategory();
        
        assertEquals(2, counts.get(RiskCategory.LIFE));
        assertEquals(2, counts.get(RiskCategory.HEALTH));
        assertEquals(3, counts.get(RiskCategory.PROPERTY));
    }

    @Test
    void testCalculateAverageRisk() {
        derivative.addObligation(lifeInsurance); // 0.5
        derivative.addObligation(healthInsurance); // 0.3
        derivative.addObligation(propertyInsurance); // 0.4
        
        double average = derivative.calculateAverageRisk();
        assertEquals((0.5 + 0.3 + 0.4) / 3, average, 0.001);
    }

    @Test
    void testCountObligationsByType() {
        derivative.addObligation(lifeInsurance);
        derivative.addObligation(healthInsurance);
        derivative.addObligation(propertyInsurance);
        
        Map<String, Long> counts = derivative.countObligationsByType();
        
        assertEquals(1, counts.get("LifeInsurance"));
        assertEquals(1, counts.get("HealthInsurance"));
        assertEquals(1, counts.get("PropertyInsurance"));
    }

    @Test
    void testGetActiveObligations() {
        lifeInsurance.setStatus(InsuranceObligation.ObligationStatus.ACTIVE);
        healthInsurance.setStatus(InsuranceObligation.ObligationStatus.DRAFT);
        
        derivative.addObligation(lifeInsurance);
        derivative.addObligation(healthInsurance);
        
        List<InsuranceObligation> active = derivative.getActiveObligations();
        assertEquals(1, active.size());
        assertEquals(lifeInsurance, active.get(0));
    }

    @Test
    void testEqualsAndHashCode() {
        Derivative d1 = new Derivative("Test");
        d1.setId(1);
        
        Derivative d2 = new Derivative("Another");
        d2.setId(1);
        
        Derivative d3 = new Derivative("Test");
        d3.setId(2);
        
        assertEquals(d1, d2);
        assertNotEquals(d1, d3);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void testToString() {
        derivative.setId(1);
        String str = derivative.toString();
        
        assertTrue(str.contains("Derivative"));
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("name='Test Derivative'"));
    }
}