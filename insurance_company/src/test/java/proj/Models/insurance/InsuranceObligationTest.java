package proj.Models.insurance;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.Set;
import proj.Models.Risk;

class InsuranceObligationTest {

    // Загальні тести для InsuranceObligation
    @Test
    void testInsuranceObligationConstructorAndGetters() {
        InsuranceObligation obligation = new LifeInsurance(0.5, 1000.0, 12);
        
        assertNotNull(obligation.getPolicyNumber());
        assertEquals(0.5, obligation.getRiskLevel());
        assertEquals(1000.0, obligation.getAmount());
        assertEquals(12, obligation.getDurationMonths());
        assertEquals(InsuranceObligation.ObligationStatus.DRAFT, obligation.getStatus());
        assertNotNull(obligation.getStartDate());
        assertNotNull(obligation.getEndDate());
        assertNotNull(obligation.getCreatedAt());
        assertNotNull(obligation.getUpdatedAt());
    }

    @Test
    void testValidateRiskLevel() {
        assertThrows(IllegalArgumentException.class, () -> new LifeInsurance(-0.1, 1000.0, 12));
        assertThrows(IllegalArgumentException.class, () -> new LifeInsurance(1.1, 1000.0, 12));
    }

    @Test
    void testValidateAmount() {
        assertThrows(IllegalArgumentException.class, () -> new LifeInsurance(0.5, -1000.0, 12));
        assertThrows(IllegalArgumentException.class, () -> new LifeInsurance(0.5, 0.0, 12));
    }

    @Test
    void testValidateDuration() {
        assertThrows(IllegalArgumentException.class, () -> new LifeInsurance(0.5, 1000.0, 0));
        assertThrows(IllegalArgumentException.class, () -> new LifeInsurance(0.5, 1000.0, -12));
    }

    @Test
    void testStatusTransitions() {
        LifeInsurance insurance = new LifeInsurance(0.5, 1000.0, 12);
        
        insurance.activate();
        assertEquals(InsuranceObligation.ObligationStatus.ACTIVE, insurance.getStatus());
        
        insurance.cancel();
        assertEquals(InsuranceObligation.ObligationStatus.CANCELLED, insurance.getStatus());
    }

    @Test
    void testRenew() {
        LifeInsurance insurance = new LifeInsurance(0.5, 1000.0, 12);
        insurance.activate();
        
        LocalDateTime originalEndDate = insurance.getEndDate();
        insurance.renew(6);
        
        assertEquals(18, insurance.getDurationMonths());
        assertEquals(originalEndDate.plusMonths(6), insurance.getEndDate());
    }

    @Test
    void testCoveredRisks() {
        LifeInsurance insurance = new LifeInsurance(0.5, 1000.0, 12, "Beneficiary", true, true);
        
        assertTrue(insurance.coversRisk("DEATH01"));
        assertTrue(insurance.coversRisk("CRIL01"));
        assertTrue(insurance.coversRisk("ACCD01"));
        
        Risk risk = new Risk("TEST01", "Test", "Test risk", 0.1, Risk.RiskCategory.LIFE);
        insurance.addRisk(risk);
        assertTrue(insurance.coversRisk("TEST01"));
        
        insurance.removeRisk(risk);
        assertFalse(insurance.coversRisk("TEST01"));
    }

    // Тести для LifeInsurance
    @Test
    void testLifeInsuranceConstructor() {
        LifeInsurance insurance = new LifeInsurance(0.5, 1000.0, 12, "Beneficiary", true, true);
        
        assertEquals("LIFE", insurance.getType());
        assertEquals("Beneficiary", insurance.getBeneficiary());
        assertTrue(insurance.includesCriticalIllness());
        assertTrue(insurance.includesAccidentalDeath());
        
        Set<Risk> risks = insurance.getCoveredRisks();
        assertEquals(3, risks.size());
    }

    @Test
    void testLifeInsuranceSetters() {
        LifeInsurance insurance = new LifeInsurance(0.5, 1000.0, 12);
        
        insurance.setBeneficiary("New Beneficiary");
        assertEquals("New Beneficiary", insurance.getBeneficiary());
        
        insurance.setIncludesCriticalIllness(true);
        assertTrue(insurance.includesCriticalIllness());
        
        insurance.setIncludesAccidentalDeath(true);
        assertTrue(insurance.includesAccidentalDeath());
    }

    @Test
    void testSetRiskLevel() {
        LifeInsurance insurance = new LifeInsurance(0.5, 1000.0, 12);
        insurance.setRiskLevel(0.8);
        assertEquals(0.8, insurance.getRiskLevel());

        assertThrows(IllegalArgumentException.class, () -> insurance.setRiskLevel(-0.1));
        assertThrows(IllegalArgumentException.class, () -> insurance.setRiskLevel(1.1));
    }

    @Test
    void testSetAmount() {
        LifeInsurance insurance = new LifeInsurance(0.5, 1000.0, 12);
        insurance.setAmount(2000.0);
        assertEquals(2000.0, insurance.getAmount());

        assertThrows(IllegalArgumentException.class, () -> insurance.setAmount(0));
        assertThrows(IllegalArgumentException.class, () -> insurance.setAmount(-100));
    }

    @Test
    void testSetRisks() {
        LifeInsurance insurance = new LifeInsurance(0.5, 1000.0, 12);
        Risk risk1 = new Risk("RISK1", "Test1", "Desc1", 0.1, Risk.RiskCategory.LIFE);
        Risk risk2 = new Risk("RISK2", "Test2", "Desc2", 0.2, Risk.RiskCategory.LIFE);

        Set<Risk> risks = new java.util.HashSet<>();
        risks.add(risk1);
        risks.add(risk2);

        insurance.setRisks(risks);
        assertEquals(2, insurance.getCoveredRisks().size());
        assertTrue(insurance.getCoveredRisks().contains(risk1));
        assertTrue(insurance.getCoveredRisks().contains(risk2));
    }

    // Тести для PropertyInsurance
    @Test
    void testPropertyInsuranceConstructor() {
        PropertyInsurance insurance = new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, false, "APARTMENT", true);
        
        assertEquals("PROPERTY", insurance.getType());
        assertEquals("Kyiv", insurance.getPropertyLocation());
        assertEquals(100000.0, insurance.getPropertyValue());
        assertFalse(insurance.isHighRiskArea());
        assertEquals("APARTMENT", insurance.getPropertyType());
        assertTrue(insurance.includesNaturalDisasters());
        
        Set<Risk> risks = insurance.getCoveredRisks();
        assertEquals(3, risks.size());
    }

    @Test
    void testPropertyInsuranceValidation() {
        assertThrows(IllegalArgumentException.class, () -> 
            new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", -100000.0, false, "APARTMENT", true));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, false, "INVALID_TYPE", true));
    }

    @Test
    void testPropertyInsuranceSetters() {
        PropertyInsurance insurance = new PropertyInsurance(0.3, 50000.0, 24);
        
        insurance.setPropertyLocation("Lviv");
        assertEquals("Lviv", insurance.getPropertyLocation());
        
        insurance.setPropertyValue(200000.0);
        assertEquals(200000.0, insurance.getPropertyValue());
        
        insurance.setHighRiskArea(true);
        assertTrue(insurance.isHighRiskArea());
        
        insurance.setPropertyType("COMMERCIAL");
        assertEquals("COMMERCIAL", insurance.getPropertyType());
        
        insurance.setIncludesNaturalDisasters(false);
        assertFalse(insurance.includesNaturalDisasters());
    }

    // Тести для HealthInsurance
    @Test
    void testHealthInsuranceConstructor() {
        HealthInsurance insurance = new HealthInsurance(0.4, 2000.0, 12, 35, true, 10000, true, false);
        
        assertEquals("HEALTH", insurance.getType());
        assertEquals(35, insurance.getAge());
        assertTrue(insurance.hasPreexistingConditions());
        assertEquals(10000, insurance.getCoverageLimit());
        assertTrue(insurance.includesHospitalization());
        assertFalse(insurance.includesDentalCare());
        
        Set<Risk> risks = insurance.getCoveredRisks();
        assertEquals(2, risks.size());
    }

    @Test
    void testSetDurationMonths() {
        LifeInsurance insurance = new LifeInsurance(0.5, 1000.0, 12);
        insurance.setDurationMonths(24);
        assertEquals(24, insurance.getDurationMonths());
    }

    @Test
    void testHealthInsuranceValidation() {
        assertThrows(IllegalArgumentException.class, () -> 
            new HealthInsurance(0.4, 2000.0, 12, -5, true, 10000, true, false));
    }

    @Test
    void testHealthInsuranceSetters() {
        HealthInsurance insurance = new HealthInsurance(0.4, 2000.0, 12);
        
        insurance.setAge(40);
        assertEquals(40, insurance.getAge());
        
        insurance.setHasPreexistingConditions(true);
        assertTrue(insurance.hasPreexistingConditions());
        
        insurance.setCoverageLimit(15000);
        assertEquals(15000, insurance.getCoverageLimit());
        
        insurance.setIncludesHospitalization(true);
        assertTrue(insurance.includesHospitalization());
        
        insurance.setIncludesDentalCare(true);
        assertTrue(insurance.includesDentalCare());
    }

    @Test
    void testAsMethod() {
        InsuranceObligation obligation = new LifeInsurance(0.5, 1000.0, 12);
        LifeInsurance lifeInsurance = obligation.as(LifeInsurance.class);
        assertNotNull(lifeInsurance);
        
        assertThrows(ClassCastException.class, () -> obligation.as(PropertyInsurance.class));
    }

    @Test
    void testEqualsAndHashCode() {
        LifeInsurance insurance1 = new LifeInsurance(0.5, 1000.0, 12);
        insurance1.setId(1);
        
        LifeInsurance insurance2 = new LifeInsurance(0.5, 1000.0, 12);
        insurance2.setId(1);
        
        LifeInsurance insurance3 = new LifeInsurance(0.6, 1500.0, 24);
        insurance3.setId(2);
        
        assertEquals(insurance1, insurance2);
        assertEquals(insurance1.hashCode(), insurance2.hashCode());
        assertNotEquals(insurance1, insurance3);
    }

    @Test
    void testToString() {
        LifeInsurance insurance = new LifeInsurance(0.5, 1000.0, 12);
        insurance.setId(1);
        insurance.setPolicyNumber("POL-123");
        
        String str = insurance.toString();
        assertTrue(str.contains("LifeInsurance"));
        assertTrue(str.contains("id=1"));
        assertTrue(str.contains("policyNumber='POL-123'"));
        assertTrue(str.contains("duration=12 months"));
    }
}