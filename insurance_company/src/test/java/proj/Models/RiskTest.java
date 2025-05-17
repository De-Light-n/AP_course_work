package proj.Models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import proj.Models.Risk.RiskCategory;

class RiskTest {
    private Risk testRisk;

    @BeforeEach
    void setUp() {
        testRisk = new Risk("TEST01", "Test Risk", "Test Description", 0.15, RiskCategory.HEALTH);
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("TEST01", testRisk.getCode());
        assertEquals("Test Risk", testRisk.getName());
        assertEquals("Test Description", testRisk.getDescription());
        assertEquals(0.15, testRisk.getBaseRiskFactor(), 0.001);
        assertEquals(RiskCategory.HEALTH, testRisk.getCategory());
    }

    @Test
    void testSetters() {
        testRisk.setName("Updated Name");
        assertEquals("Updated Name", testRisk.getName());

        testRisk.setDescription("Updated Description");
        assertEquals("Updated Description", testRisk.getDescription());

        testRisk.setBaseRiskFactor(0.2);
        assertEquals(0.2, testRisk.getBaseRiskFactor(), 0.001);

        testRisk.setCategory(RiskCategory.LIFE);
        assertEquals(RiskCategory.LIFE, testRisk.getCategory());
    }

    @Test
    void testValidateCode() {
        // Тест валідного коду
        assertDoesNotThrow(() -> new Risk("AB12", "Valid", "Desc", 0.1, RiskCategory.PROPERTY));

        // Тест невалідних кодів
        assertThrows(IllegalArgumentException.class, 
            () -> new Risk(null, "Null", "Desc", 0.1, RiskCategory.PROPERTY));
        
        assertThrows(IllegalArgumentException.class, 
            () -> new Risk("A", "Too short", "Desc", 0.1, RiskCategory.PROPERTY));
    }

    @Test
    void testValidateRiskFactor() {
        // Тест валідного фактору ризику
        assertDoesNotThrow(() -> new Risk("TEST", "Valid", "Desc", 0.01, RiskCategory.HEALTH));

        // Тест невалідних факторів
        assertThrows(IllegalArgumentException.class, 
            () -> new Risk("TEST", "Invalid", "Desc", 0, RiskCategory.HEALTH));
        
        assertThrows(IllegalArgumentException.class, 
            () -> new Risk("TEST", "Invalid", "Desc", -0.1, RiskCategory.HEALTH));
    }

    @Test
    void testEqualsAndHashCode() {
        Risk sameRisk = new Risk("TEST01", "Different Name", "Different Desc", 0.2, RiskCategory.LIFE);
        Risk differentRisk = new Risk("DIFF01", "Different", "Different", 0.1, RiskCategory.PROPERTY);

        assertEquals(testRisk, sameRisk);
        assertNotEquals(testRisk, differentRisk);
        assertEquals(testRisk.hashCode(), sameRisk.hashCode());
        assertNotEquals(testRisk.hashCode(), differentRisk.hashCode());
    }

    @Test
    void testToString() {
        String str = testRisk.toString();
        assertTrue(str.contains("Risk"));
        assertTrue(str.contains("code='TEST01'"));
        assertTrue(str.contains("name='Test Risk'"));
        assertTrue(str.contains("category=HEALTH"));
    }

    @Test
    void testNullValuesInConstructor() {
        assertThrows(NullPointerException.class, 
            () -> new Risk("TEST", null, "Desc", 0.1, RiskCategory.HEALTH));
        
        assertThrows(NullPointerException.class, 
            () -> new Risk("TEST", "Name", "Desc", 0.1, null));
    }

    @Test
    void testNullValuesInSetters() {
        assertThrows(NullPointerException.class, 
            () -> testRisk.setName(null));
        
        assertThrows(NullPointerException.class, 
            () -> testRisk.setCategory(null));
    }

    @Test
    void testCodeNormalization() {
        Risk lowerCaseRisk = new Risk("test01", "Test", "Desc", 0.1, RiskCategory.HEALTH);
        assertEquals("TEST01", lowerCaseRisk.getCode());
    }
}