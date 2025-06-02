package proj.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import proj.Models.Derivative;
import proj.Models.insurance.HealthInsurance;
import proj.Models.insurance.InsuranceObligation;
import proj.Models.insurance.LifeInsurance;
import proj.Models.insurance.PropertyInsurance;
import proj.Models.Risk;
import proj.Models.Risk.RiskCategory;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DerivativeServiceTest {
    private Derivative derivative;
    private InsuranceObligation lifeInsurance;
    private InsuranceObligation healthInsurance;
    private InsuranceObligation propertyInsurance;

    @BeforeEach
    void setUp() {
        derivative = new Derivative("Test Derivative");
        lifeInsurance = new LifeInsurance(0.5, 100000.0, 12, "John Doe", true, false);
        healthInsurance = new HealthInsurance(0.3, 50000.0, 6, 35, false, 200000, true, false);
        propertyInsurance = new PropertyInsurance(0.4, 500000.0, 24, "Kyiv", 1000000.0, false, "APARTMENT", true);

        lifeInsurance.addRisk(new Risk("DEATH01", "Death", "Risk of death", 0.25, RiskCategory.LIFE));
        healthInsurance.addRisk(new Risk("HLTH01", "Health", "Health risk", 0.2, RiskCategory.HEALTH));
        propertyInsurance.addRisk(new Risk("FIRE01", "Fire", "Fire risk", 0.15, RiskCategory.PROPERTY));
    }

    @Test
    void testSortByRiskLevel() {
        derivative.addObligation(lifeInsurance);
        derivative.addObligation(healthInsurance);
        derivative.addObligation(propertyInsurance);

        DerivativeService.getInstance().sortByRiskLevel(derivative);

        List<InsuranceObligation> sorted = derivative.getObligations();
        assertEquals(0.5, sorted.get(0).getRiskLevel(), 0.001);
        assertEquals(0.4, sorted.get(1).getRiskLevel(), 0.001);
        assertEquals(0.3, sorted.get(2).getRiskLevel(), 0.001);
    }

    @Test
    void testFindObligationsInRange() {
        derivative.addObligation(lifeInsurance);
        derivative.addObligation(healthInsurance);
        derivative.addObligation(propertyInsurance);

        List<InsuranceObligation> result = DerivativeService.getInstance()
                .findObligationsInRange(derivative, 0.3, 0.4, 40000, 60000);

        assertEquals(1, result.size());
        assertEquals(healthInsurance, result.get(0));
    }

    @Test
    void testCountRisksByCategory() {
        derivative.addObligation(lifeInsurance);
        derivative.addObligation(healthInsurance);
        derivative.addObligation(propertyInsurance);

        Map<RiskCategory, Long> counts = DerivativeService.getInstance().countRisksByCategory(derivative);

        assertEquals(2, counts.get(RiskCategory.LIFE));
        assertEquals(2, counts.get(RiskCategory.HEALTH));
        assertEquals(3, counts.get(RiskCategory.PROPERTY));
    }

    @Test
    void testCalculateAverageRisk() {
        derivative.addObligation(lifeInsurance);
        derivative.addObligation(healthInsurance);
        derivative.addObligation(propertyInsurance);

        double average = DerivativeService.getInstance().calculateAverageRisk(derivative);
        assertEquals((0.5 + 0.3 + 0.4) / 3, average, 0.001);
    }

    @Test
    void testCountObligationsByType() {
        derivative.addObligation(lifeInsurance);
        derivative.addObligation(healthInsurance);
        derivative.addObligation(propertyInsurance);

        Map<String, Long> counts = DerivativeService.getInstance().countObligationsByType(derivative);

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

        List<InsuranceObligation> active = DerivativeService.getInstance().getActiveObligations(derivative);
        assertEquals(1, active.size());
        assertEquals(lifeInsurance, active.get(0));
    }

    @Test
    void testFilterDerivatives() {
        Derivative d1 = new Derivative("Alpha");
        Derivative d2 = new Derivative("Beta");
        d1.setTotalValue(1000);
        d2.setTotalValue(5000);

        List<Derivative> all = List.of(d1, d2);

        // Фільтр за назвою
        List<Derivative> filtered = DerivativeService.getInstance().filterDerivatives(all, "alp", "", "");
        assertEquals(1, filtered.size());
        assertEquals("Alpha", filtered.get(0).getName());

        // Фільтр за діапазоном вартості
        filtered = DerivativeService.getInstance().filterDerivatives(all, "", "2000", "6000");
        assertEquals(1, filtered.size());
        assertEquals("Beta", filtered.get(0).getName());
    }

    @Test
    void testSortDerivatives() {
        Derivative d1 = new Derivative("Alpha");
        Derivative d2 = new Derivative("Beta");
        d1.setTotalValue(1000);
        d2.setTotalValue(5000);
        d1.setCreatedAt(d1.getCreatedAt().minusDays(1));
        d2.setCreatedAt(d2.getCreatedAt());

        List<Derivative> list = List.of(d2, d1);
        List<Derivative> toSort = new java.util.ArrayList<>(list);

        DerivativeService.getInstance().sortDerivatives(toSort, "Назвою (А-Я)");
        assertEquals("Alpha", toSort.get(0).getName());

        DerivativeService.getInstance().sortDerivatives(toSort, "Вартістю (спадання)");
        assertEquals("Beta", toSort.get(0).getName());

        DerivativeService.getInstance().sortDerivatives(toSort, "Датою (старіші)");
        assertEquals("Alpha", toSort.get(0).getName());
    }

    @Test
    void testSortDerivativesByNameAsc() {
        Derivative d1 = new Derivative("Alpha");
        Derivative d2 = new Derivative("Beta");
        List<Derivative> list = new java.util.ArrayList<>(List.of(d2, d1));

        DerivativeService.getInstance().sortDerivatives(list, "Назвою (А-Я)");
        assertEquals("Alpha", list.get(0).getName());
        assertEquals("Beta", list.get(1).getName());
    }

    @Test
    void testSortDerivativesByNameDesc() {
        Derivative d1 = new Derivative("Alpha");
        Derivative d2 = new Derivative("Beta");
        List<Derivative> list = new java.util.ArrayList<>(List.of(d1, d2));

        DerivativeService.getInstance().sortDerivatives(list, "Назвою (Я-А)");
        assertEquals("Beta", list.get(0).getName());
        assertEquals("Alpha", list.get(1).getName());
    }

    @Test
    void testSortDerivativesByValueAsc() {
        Derivative d1 = new Derivative("Alpha");
        Derivative d2 = new Derivative("Beta");
        d1.setTotalValue(1000);
        d2.setTotalValue(5000);
        List<Derivative> list = new java.util.ArrayList<>(List.of(d2, d1));

        DerivativeService.getInstance().sortDerivatives(list, "Вартістю (зростання)");
        assertEquals("Alpha", list.get(0).getName());
        assertEquals("Beta", list.get(1).getName());
    }

    @Test
    void testSortDerivativesByValueDesc() {
        Derivative d1 = new Derivative("Alpha");
        Derivative d2 = new Derivative("Beta");
        d1.setTotalValue(1000);
        d2.setTotalValue(5000);
        List<Derivative> list = new java.util.ArrayList<>(List.of(d1, d2));

        DerivativeService.getInstance().sortDerivatives(list, "Вартістю (спадання)");
        assertEquals("Beta", list.get(0).getName());
        assertEquals("Alpha", list.get(1).getName());
    }

    @Test
    void testSortDerivativesByDateNewest() {
        Derivative d1 = new Derivative("Alpha");
        Derivative d2 = new Derivative("Beta");
        d1.setCreatedAt(d1.getCreatedAt().minusDays(2));
        d2.setCreatedAt(d2.getCreatedAt().minusDays(1));
        List<Derivative> list = new java.util.ArrayList<>(List.of(d1, d2));

        DerivativeService.getInstance().sortDerivatives(list, "Датою (новіші)");
        assertEquals("Beta", list.get(0).getName());
        assertEquals("Alpha", list.get(1).getName());
    }

    @Test
    void testSortDerivativesByDateOldest() {
        Derivative d1 = new Derivative("Alpha");
        Derivative d2 = new Derivative("Beta");
        d1.setCreatedAt(d1.getCreatedAt().minusDays(2));
        d2.setCreatedAt(d2.getCreatedAt().minusDays(1));
        List<Derivative> list = new java.util.ArrayList<>(List.of(d2, d1));

        DerivativeService.getInstance().sortDerivatives(list, "Датою (старіші)");
        assertEquals("Alpha", list.get(0).getName());
        assertEquals("Beta", list.get(1).getName());
    }
}
