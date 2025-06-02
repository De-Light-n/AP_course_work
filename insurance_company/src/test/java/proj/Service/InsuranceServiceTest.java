package proj.Service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import proj.Models.insurance.LifeInsurance;
import proj.Models.insurance.PropertyInsurance;
import proj.Models.insurance.HealthInsurance;
import proj.Models.insurance.InsuranceObligation;

import java.util.*;

public class InsuranceServiceTest {

    @Test
    void testLifeInsuranceCalculateValue() {
        LifeInsurance insurance = new LifeInsurance(0.5, 1000.0, 12, "Beneficiary", true, true);
        double value = InsuranceService.getInstance().calculateLifeInsuranceValue(insurance);

        assertTrue(value > 1000.0); // Базове значення має бути більше суми
        assertEquals(value, insurance.getCalculatedValue());
    }

    @Test
    void testPropertyInsuranceCalculateValue() {
        PropertyInsurance insurance = new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, true, "HOUSE", true);
        double value = InsuranceService.getInstance().calculatePropertyInsuranceValue(insurance);

        assertTrue(value > 50000.0);
        assertEquals(value, insurance.getCalculatedValue());
    }

    @Test
    void testHealthInsuranceCalculateValue() {
        HealthInsurance insurance = new HealthInsurance(0.4, 2000.0, 12, 55, true, 10000, true, true);
        double value = InsuranceService.getInstance().calculateHealthInsuranceValue(insurance);

        assertTrue(value > 2000.0);
        assertEquals(value, insurance.getCalculatedValue());
    }

    @Test
    void testCalculateTotalObligationsValue() {
        LifeInsurance l = new LifeInsurance(0.5, 1000.0, 12, "B", true, false);
        PropertyInsurance p = new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        HealthInsurance h = new HealthInsurance(0.4, 2000.0, 12, 30, false, 10000, false, false);

        List<InsuranceObligation> list = List.of(l, p, h);
        double total = InsuranceService.getInstance().calculateTotalObligationsValue(list);

        assertTrue(total > 0);
        assertEquals(total, l.getCalculatedValue() + p.getCalculatedValue() + h.getCalculatedValue(), 0.0001);
    }

    @Test
    void testCalculateObligationValueDispatch() {
        LifeInsurance l = new LifeInsurance(0.5, 1000.0, 12, "B", false, false);
        double v1 = InsuranceService.getInstance().calculateObligationValue(l);
        assertEquals(l.getCalculatedValue(), v1);

        PropertyInsurance p = new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        double v2 = InsuranceService.getInstance().calculateObligationValue(p);
        assertEquals(p.getCalculatedValue(), v2);

        HealthInsurance h = new HealthInsurance(0.4, 2000.0, 12, 30, false, 10000, false, false);
        double v3 = InsuranceService.getInstance().calculateObligationValue(h);
        assertEquals(h.getCalculatedValue(), v3);
    }

    @Test
    void testGetActiveObligations() {
        LifeInsurance l = new LifeInsurance(0.5, 1000.0, 12, "B", false, false);
        l.setStatus(InsuranceObligation.ObligationStatus.ACTIVE);
        PropertyInsurance p = new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        p.setStatus(InsuranceObligation.ObligationStatus.DRAFT);

        List<InsuranceObligation> list = List.of(l, p);
        List<InsuranceObligation> active = InsuranceService.getInstance().getActiveObligations(list);

        assertEquals(1, active.size());
        assertEquals(l, active.get(0));
    }

    @Test
    void testGroupByStatus() {
        LifeInsurance l = new LifeInsurance(0.5, 1000.0, 12, "B", false, false);
        l.setStatus(InsuranceObligation.ObligationStatus.ACTIVE);
        PropertyInsurance p = new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        p.setStatus(InsuranceObligation.ObligationStatus.DRAFT);

        Map<InsuranceObligation.ObligationStatus, List<InsuranceObligation>> grouped = InsuranceService.getInstance()
                .groupByStatus(List.of(l, p));

        assertEquals(1, grouped.get(InsuranceObligation.ObligationStatus.ACTIVE).size());
        assertEquals(1, grouped.get(InsuranceObligation.ObligationStatus.DRAFT).size());
    }

    @Test
    void testGroupByType() {
        LifeInsurance l = new LifeInsurance(0.5, 1000.0, 12, "B", false, false);
        PropertyInsurance p = new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        List<InsuranceObligation> list = List.of(p, l);

        Map<String, List<InsuranceObligation>> grouped = InsuranceService.getInstance().groupByType(List.of(l, p));

        assertEquals(1, grouped.get("LIFE").size());
        assertEquals(1, grouped.get("PROPERTY").size());
    }

    @Test
    void testGetAllCoveredRisks() {
        LifeInsurance l = new LifeInsurance(0.5, 1000.0, 12, "B", true, false);
        PropertyInsurance p = new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", true);

        Set<proj.Models.Risk> risks = InsuranceService.getInstance().getAllCoveredRisks(List.of(l, p));
        assertTrue(risks.size() >= 2);
    }

    @Test
    void testGroupRisksByCategory() {
        LifeInsurance l = new LifeInsurance(0.5, 1000.0, 12, "B", true, false);
        PropertyInsurance p = new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", true);

        Set<proj.Models.Risk> risks = InsuranceService.getInstance().getAllCoveredRisks(List.of(l, p));
        Map<proj.Models.Risk.RiskCategory, List<proj.Models.Risk>> grouped = InsuranceService.getInstance()
                .groupRisksByCategory(risks);

        assertTrue(grouped.containsKey(proj.Models.Risk.RiskCategory.LIFE));
        assertTrue(grouped.containsKey(proj.Models.Risk.RiskCategory.PROPERTY));
    }

    @Test
    void testCountObligationsByRisk() {
        LifeInsurance l = new LifeInsurance(0.5, 1000.0, 12, "B", true, false);
        PropertyInsurance p = new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", true);

        Map<proj.Models.Risk, Long> counts = InsuranceService.getInstance().countObligationsByRisk(List.of(l, p));
        assertTrue(counts.values().stream().anyMatch(v -> v >= 1));
    }

    // --- Тести сортування для всіх типів ---

    @Test
    void testSortObligationsByPolicyNumberAsc() {
        LifeInsurance l = new LifeInsurance(0.5, 1000.0, 12, "B", false, false);
        l.setPolicyNumber("A123");
        PropertyInsurance p = new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        p.setPolicyNumber("B456");
        List<InsuranceObligation> list = List.of(p, l);

        List<InsuranceObligation> sorted = InsuranceService.getInstance().sortObligations(list,
                "Номер полісу (зростання)");
        assertEquals("A123", sorted.get(0).getPolicyNumber());
    }

    @Test
    void testSortObligationsByPolicyNumberDesc() {
        LifeInsurance l = new LifeInsurance(0.5, 1000.0, 12, "B", false, false);
        l.setPolicyNumber("A123");
        PropertyInsurance p = new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        p.setPolicyNumber("B456");
        List<InsuranceObligation> list = List.of(l, p);

        List<InsuranceObligation> sorted = InsuranceService.getInstance().sortObligations(list,
                "Номер полісу (спадання)");
        assertEquals("B456", sorted.get(0).getPolicyNumber());
    }

    @Test
    void testSortObligationsByType() {
        LifeInsurance l = new LifeInsurance(0.5, 1000.0, 12, "B", false, false);
        PropertyInsurance p = new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        List<InsuranceObligation> list = List.of(p, l);

        List<InsuranceObligation> sorted = InsuranceService.getInstance().sortObligations(list, "Тип");
        assertEquals("LIFE", sorted.get(0).getType());
    }

    @Test
    void testSortObligationsByRiskLevelAsc() {
        LifeInsurance l = new LifeInsurance(0.2, 1000.0, 12, "B", false, false);
        PropertyInsurance p = new PropertyInsurance(0.5, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        List<InsuranceObligation> list = List.of(p, l);

        List<InsuranceObligation> sorted = InsuranceService.getInstance().sortObligations(list,
                "Рівень ризику (зростання)");
        assertEquals(0.2, sorted.get(0).getRiskLevel(), 0.001);
    }

    @Test
    void testSortObligationsByRiskLevelDesc() {
        LifeInsurance l = new LifeInsurance(0.2, 1000.0, 12, "B", false, false);
        PropertyInsurance p = new PropertyInsurance(0.5, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        List<InsuranceObligation> list = List.of(l, p);

        List<InsuranceObligation> sorted = InsuranceService.getInstance().sortObligations(list,
                "Рівень ризику (спадання)");
        assertEquals(0.5, sorted.get(0).getRiskLevel(), 0.001);
    }

    @Test
    void testSortObligationsByAmountAsc() {
        LifeInsurance l = new LifeInsurance(0.2, 1000.0, 12, "B", false, false);
        PropertyInsurance p = new PropertyInsurance(0.5, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        List<InsuranceObligation> list = List.of(p, l);

        List<InsuranceObligation> sorted = InsuranceService.getInstance().sortObligations(list, "Сума (зростання)");
        assertEquals(1000.0, sorted.get(0).getAmount(), 0.001);
    }

    @Test
    void testSortObligationsByAmountDesc() {
        LifeInsurance l = new LifeInsurance(0.2, 1000.0, 12, "B", false, false);
        PropertyInsurance p = new PropertyInsurance(0.5, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        List<InsuranceObligation> list = List.of(l, p);

        List<InsuranceObligation> sorted = InsuranceService.getInstance().sortObligations(list, "Сума (спадання)");
        assertEquals(50000.0, sorted.get(0).getAmount(), 0.001);
    }

    @Test
    void testSortObligationsByCalculatedValueAsc() {
        LifeInsurance l = new LifeInsurance(0.2, 1000.0, 12, "B", false, false);
        PropertyInsurance p = new PropertyInsurance(0.5, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        InsuranceService.getInstance().calculateObligationValue(l);
        InsuranceService.getInstance().calculateObligationValue(p);
        List<InsuranceObligation> list = List.of(p, l);

        List<InsuranceObligation> sorted = InsuranceService.getInstance().sortObligations(list,
                "Розрах. вартість (зростання)");
        assertTrue(sorted.get(0).getCalculatedValue() <= sorted.get(1).getCalculatedValue());
    }

    @Test
    void testSortObligationsByCalculatedValueDesc() {
        LifeInsurance l = new LifeInsurance(0.2, 1000.0, 12, "B", false, false);
        PropertyInsurance p = new PropertyInsurance(0.5, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        InsuranceService.getInstance().calculateObligationValue(l);
        InsuranceService.getInstance().calculateObligationValue(p);
        List<InsuranceObligation> list = List.of(l, p);

        List<InsuranceObligation> sorted = InsuranceService.getInstance().sortObligations(list,
                "Розрах. вартість (спадання)");
        assertTrue(sorted.get(0).getCalculatedValue() >= sorted.get(1).getCalculatedValue());
    }

    @Test
    void testSortObligationsByStatus() {
        LifeInsurance l = new LifeInsurance(0.2, 1000.0, 12, "B", false, false);
        l.setStatus(InsuranceObligation.ObligationStatus.ACTIVE);
        PropertyInsurance p = new PropertyInsurance(0.5, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        p.setStatus(InsuranceObligation.ObligationStatus.DRAFT);
        List<InsuranceObligation> list = List.of(p, l);

        List<InsuranceObligation> sorted = InsuranceService.getInstance().sortObligations(list, "Статус");
        assertEquals(InsuranceObligation.ObligationStatus.ACTIVE, sorted.get(0).getStatus());
    }

    @Test
    void testFilterAndSortObligations() {
        LifeInsurance l = new LifeInsurance(0.5, 1000.0, 12, "B", false, false);
        l.setPolicyNumber("LIFE123");
        l.setCalculatedValue(1500.0);
        PropertyInsurance p = new PropertyInsurance(0.3, 50000.0, 24, "Kyiv", 100000.0, false, "HOUSE", false);
        p.setPolicyNumber("PROP456");
        p.setCalculatedValue(60000.0);

        List<InsuranceObligation> list = List.of(l, p);

        List<InsuranceObligation> filteredSorted = InsuranceService.getInstance()
                .filterAndSortObligations(list, "PROP", "50000", "70000", "Номер полісу (зростання)");
        assertEquals(1, filteredSorted.size());
        assertEquals("PROP456", filteredSorted.get(0).getPolicyNumber());
    }
}
