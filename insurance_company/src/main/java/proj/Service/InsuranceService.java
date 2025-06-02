package proj.Service;

import java.util.*;
import java.util.stream.Collectors;

import proj.Models.insurance.HealthInsurance;
import proj.Models.insurance.InsuranceObligation;
import proj.Models.insurance.LifeInsurance;
import proj.Models.insurance.PropertyInsurance;
import proj.Models.Risk;

/**
 * Сервіс для розрахунку, фільтрації та сортування страхових зобов'язань.
 * Містить бізнес-логіку для всіх типів страхування.
 */
public class InsuranceService {
    private static final InsuranceService INSTANCE = new InsuranceService();

    private InsuranceService() {
    }

    /**
     * Повертає єдиний екземпляр сервісу.
     *
     * @return екземпляр InsuranceService
     */
    public static InsuranceService getInstance() {
        return INSTANCE;
    }

    /**
     * Обчислює вартість страхування здоров'я.
     *
     * @param insurance об'єкт HealthInsurance
     * @return розрахована вартість
     */
    public double calculateHealthInsuranceValue(HealthInsurance insurance) {
        double baseValue = insurance.getAmount() * (1 + insurance.getRiskLevel() * 0.05);

        if (insurance.getAge() > 50) {
            baseValue *= 1.5;
        } else if (insurance.getAge() > 30) {
            baseValue *= 1.2;
        }

        if (insurance.hasPreexistingConditions()) {
            baseValue *= 1.8;
        }

        if (insurance.includesHospitalization()) {
            baseValue *= 1.3;
        }

        if (insurance.includesDentalCare()) {
            baseValue *= 1.1;
        }

        double riskFactor = insurance.getCoveredRisks().stream()
                .mapToDouble(Risk::getBaseRiskFactor)
                .sum();
        baseValue *= (1 + riskFactor);

        insurance.setCalculatedValue(baseValue);
        return baseValue;
    }

    /**
     * Обчислює вартість страхування життя.
     *
     * @param insurance об'єкт LifeInsurance
     * @return розрахована вартість
     */
    public double calculateLifeInsuranceValue(LifeInsurance insurance) {
        double baseValue = insurance.getAmount() * (1 + insurance.getRiskLevel() * 0.1);

        if (insurance.includesCriticalIllness()) {
            baseValue *= 1.3;
        }
        if (insurance.includesAccidentalDeath()) {
            baseValue *= 1.2;
        }

        double riskFactor = insurance.getCoveredRisks().stream()
                .mapToDouble(Risk::getBaseRiskFactor)
                .sum();
        baseValue *= (1 + riskFactor);

        insurance.setCalculatedValue(baseValue);
        return baseValue;
    }

    /**
     * Обчислює вартість страхування майна.
     *
     * @param insurance об'єкт PropertyInsurance
     * @return розрахована вартість
     */
    public double calculatePropertyInsuranceValue(PropertyInsurance insurance) {
        double baseValue = insurance.getAmount() * (1 + insurance.getRiskLevel() * 0.03);

        if (insurance.isHighRiskArea()) {
            baseValue *= 1.5;
        }

        switch (insurance.getPropertyType()) {
            case "HOUSE":
                baseValue *= 1.3;
                break;
            case "COMMERCIAL":
                baseValue *= 1.7;
                break;
            default:
                break;
        }

        if (insurance.includesNaturalDisasters()) {
            baseValue *= 1.4;
        }

        double riskFactor = insurance.getCoveredRisks().stream()
                .mapToDouble(Risk::getBaseRiskFactor)
                .sum();
        baseValue *= (1 + riskFactor);

        insurance.setCalculatedValue(baseValue);
        return baseValue;
    }

    /**
     * Обчислює загальну суму страхових зобов'язань.
     *
     * @param obligations список зобов'язань
     * @return загальна розрахована вартість
     */
    public double calculateTotalObligationsValue(List<InsuranceObligation> obligations) {
        return obligations.stream()
                .mapToDouble(this::calculateObligationValue)
                .sum();
    }

    /**
     * Обчислює вартість зобов'язання за його типом.
     *
     * @param obligation страхове зобов'язання
     * @return розрахована вартість
     */
    public double calculateObligationValue(InsuranceObligation obligation) {
        if (obligation instanceof HealthInsurance) {
            return calculateHealthInsuranceValue((HealthInsurance) obligation);
        } else if (obligation instanceof LifeInsurance) {
            return calculateLifeInsuranceValue((LifeInsurance) obligation);
        } else if (obligation instanceof PropertyInsurance) {
            return calculatePropertyInsuranceValue((PropertyInsurance) obligation);
        } else {
            throw new IllegalArgumentException("Unknown insurance type");
        }
    }

    /**
     * Повертає всі активні зобов'язання.
     *
     * @param obligations список зобов'язань
     * @return список активних зобов'язань
     */
    public List<InsuranceObligation> getActiveObligations(List<InsuranceObligation> obligations) {
        return obligations.stream()
                .filter(InsuranceObligation::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Групує зобов'язання за статусом.
     *
     * @param obligations список зобов'язань
     * @return мапа статус → список зобов'язань
     */
    public Map<InsuranceObligation.ObligationStatus, List<InsuranceObligation>> groupByStatus(
            List<InsuranceObligation> obligations) {
        return obligations.stream()
                .collect(Collectors.groupingBy(InsuranceObligation::getStatus));
    }

    /**
     * Групує зобов'язання за типом.
     *
     * @param obligations список зобов'язань
     * @return мапа тип → список зобов'язань
     */
    public Map<String, List<InsuranceObligation>> groupByType(List<InsuranceObligation> obligations) {
        return obligations.stream()
                .collect(Collectors.groupingBy(InsuranceObligation::getType));
    }

    /**
     * Повертає всі ризики, які покриваються у списку зобов'язань.
     *
     * @param obligations список зобов'язань
     * @return набір ризиків
     */
    public Set<Risk> getAllCoveredRisks(List<InsuranceObligation> obligations) {
        return obligations.stream()
                .flatMap(o -> o.getCoveredRisks().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Групує ризики за категорією.
     *
     * @param risks набір ризиків
     * @return мапа категорія → список ризиків
     */
    public Map<Risk.RiskCategory, List<Risk>> groupRisksByCategory(Set<Risk> risks) {
        return risks.stream()
                .collect(Collectors.groupingBy(Risk::getCategory));
    }

    /**
     * Підраховує кількість зобов'язань для кожного ризику.
     *
     * @param obligations список зобов'язань
     * @return мапа ризик → кількість зобов'язань
     */
    public Map<Risk, Long> countObligationsByRisk(List<InsuranceObligation> obligations) {
        return obligations.stream()
                .flatMap(o -> o.getCoveredRisks().stream())
                .collect(Collectors.groupingBy(risk -> risk, Collectors.counting()));
    }

    /**
     * Фільтрує зобов'язання за текстовим пошуком та діапазоном розрахункової
     * вартості.
     *
     * @param obligations список зобов'язань
     * @param searchText  текст для пошуку
     * @param minCalc     мінімальна розрахункова вартість
     * @param maxCalc     максимальна розрахункова вартість
     * @return відфільтрований список зобов'язань
     */
    public List<InsuranceObligation> filterObligations(List<InsuranceObligation> obligations, String searchText,
            double minCalc, double maxCalc) {
        return obligations.stream()
                .filter(o -> {
                    double calcVal = o.getCalculatedValue();
                    boolean inRange = calcVal >= minCalc && calcVal <= maxCalc;
                    boolean matches = searchText.isEmpty()
                            || String.valueOf(o.getPolicyNumber()).toLowerCase().contains(searchText)
                            || o.getType().toLowerCase().contains(searchText)
                            || String.valueOf(o.getRiskLevel()).contains(searchText)
                            || String.valueOf(o.getAmount()).contains(searchText)
                            || String.valueOf(o.getCalculatedValue()).contains(searchText)
                            || o.getStatus().toString().toLowerCase().contains(searchText);
                    return inRange && matches;
                })
                .collect(Collectors.toList());
    }

    /**
     * Сортує список зобов'язань згідно з вибраним критерієм.
     *
     * @param obligations список для сортування
     * @param sortOption  критерій сортування
     * @return відсортований список зобов'язань
     */
    public List<InsuranceObligation> sortObligations(List<InsuranceObligation> obligations, String sortOption) {
        if (sortOption == null)
            return obligations;

        List<InsuranceObligation> sorted = new ArrayList<>(obligations);
        switch (sortOption) {
            case "Номер полісу (зростання)":
                sorted.sort(Comparator.comparing(InsuranceObligation::getPolicyNumber));
                break;
            case "Номер полісу (спадання)":
                sorted.sort(Comparator.comparing(InsuranceObligation::getPolicyNumber).reversed());
                break;
            case "Тип":
                sorted.sort(Comparator.comparing(InsuranceObligation::getType));
                break;
            case "Рівень ризику (зростання)":
                sorted.sort(Comparator.comparingDouble(InsuranceObligation::getRiskLevel));
                break;
            case "Рівень ризику (спадання)":
                sorted.sort(Comparator.comparingDouble(InsuranceObligation::getRiskLevel).reversed());
                break;
            case "Сума (зростання)":
                sorted.sort(Comparator.comparingDouble(InsuranceObligation::getAmount));
                break;
            case "Сума (спадання)":
                sorted.sort(Comparator.comparingDouble(InsuranceObligation::getAmount).reversed());
                break;
            case "Розрах. вартість (зростання)":
                sorted.sort(Comparator.comparingDouble(InsuranceObligation::getCalculatedValue));
                break;
            case "Розрах. вартість (спадання)":
                sorted.sort(Comparator.comparingDouble(InsuranceObligation::getCalculatedValue).reversed());
                break;
            case "Статус":
                sorted.sort(Comparator.comparing(o -> o.getStatus().toString()));
                break;
        }
        return sorted;
    }

    /**
     * Фільтрує та сортує зобов'язання згідно з параметрами.
     *
     * @param obligations список зобов'язань
     * @param searchText  текст для пошуку
     * @param minCalcStr  мінімальна розрахункова вартість (рядок)
     * @param maxCalcStr  максимальна розрахункова вартість (рядок)
     * @param sortOption  критерій сортування
     * @return відфільтрований та відсортований список зобов'язань
     */
    public List<InsuranceObligation> filterAndSortObligations(List<InsuranceObligation> obligations, String searchText,
            String minCalcStr, String maxCalcStr, String sortOption) {
        try {
            double minCalc = minCalcStr.isEmpty() ? 0 : Double.parseDouble(minCalcStr);
            double maxCalc = maxCalcStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxCalcStr);

            List<InsuranceObligation> filtered = filterObligations(obligations, searchText.toLowerCase(), minCalc,
                    maxCalc);
            return sortObligations(filtered, sortOption);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
