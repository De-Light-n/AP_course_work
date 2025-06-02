package proj.Models;

import proj.Models.insurance.InsuranceObligation;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
 * Клас Derivative представляє страховий дериватив, який містить набір страхових
 * зобов'язань.
 * Містить інформацію про назву, список зобов'язань, загальну вартість, дати
 * створення та оновлення.
 */
public class Derivative {
    private int id;
    private String name;
    private List<InsuranceObligation> obligations;
    private double totalValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Створює новий дериватив з вказаною назвою.
     *
     * @param name назва деривативу
     */
    public Derivative(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.obligations = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        this.updatedAt = LocalDateTime.now();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Повертає список страхових зобов'язань (тільки для читання).
     *
     * @return список зобов'язань
     */
    public List<InsuranceObligation> getObligations() {
        return Collections.unmodifiableList(obligations);
    }

    /**
     * Встановлює список страхових зобов'язань.
     *
     * @param obligations список зобов'язань
     */
    public void setObligations(List<InsuranceObligation> obligations) {
        this.obligations = Objects.requireNonNull(obligations, "Obligations cannot be null");
        updateDerivative();
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt cannot be null");
    }

    /**
     * Додає страхове зобов'язання до деривативу.
     *
     * @param obligation страхове зобов'язання
     */
    public void addObligation(InsuranceObligation obligation) {
        if (!obligations.contains(obligation)) {
            obligations.add(obligation);
            totalValue += obligation.calculateValue();
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Видаляє страхове зобов'язання з деривативу.
     *
     * @param obligation страхове зобов'язання
     */
    public void removeObligation(InsuranceObligation obligation) {
        obligations.remove(obligation);
        updateDerivative();
    }

    /**
     * Сортує зобов'язання за рівнем ризику (від більшого до меншого).
     */
    public void sortByRiskLevel() {
        obligations.sort((o1, o2) -> Double.compare(o2.getRiskLevel(), o1.getRiskLevel()));
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Повертає список зобов'язань, що відповідають заданим діапазонам ризику та
     * суми.
     *
     * @param minRisk   мінімальний рівень ризику
     * @param maxRisk   максимальний рівень ризику
     * @param minAmount мінімальна сума
     * @param maxAmount максимальна сума
     * @return список зобов'язань у діапазоні
     */
    public List<InsuranceObligation> findObligationsInRange(double minRisk, double maxRisk,
            double minAmount, double maxAmount) {
        return obligations.stream()
                .filter(o -> o.getRiskLevel() >= minRisk && o.getRiskLevel() <= maxRisk)
                .filter(o -> o.getAmount() >= minAmount && o.getAmount() <= maxAmount)
                .collect(Collectors.toList());
    }

    /**
     * Повертає кількість ризиків за категоріями серед усіх зобов'язань.
     *
     * @return мапа категорій ризиків та їх кількості
     */
    public Map<Risk.RiskCategory, Long> countRisksByCategory() {
        return obligations.stream()
                .flatMap(o -> o.getCoveredRisks().stream())
                .collect(Collectors.groupingBy(
                        Risk::getCategory,
                        Collectors.counting()));
    }

    /**
     * Обчислює середній рівень ризику серед усіх зобов'язань.
     *
     * @return середній рівень ризику
     */
    public double calculateAverageRisk() {
        return obligations.stream()
                .mapToDouble(InsuranceObligation::getRiskLevel)
                .average()
                .orElse(0.0);
    }

    /**
     * Повертає кількість зобов'язань за типом.
     *
     * @return мапа типів зобов'язань та їх кількості
     */
    public Map<String, Long> countObligationsByType() {
        return obligations.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getClass().getSimpleName(),
                        Collectors.counting()));
    }

    /**
     * Повертає список активних зобов'язань.
     *
     * @return список активних зобов'язань
     */
    public List<InsuranceObligation> getActiveObligations() {
        return obligations.stream()
                .filter(InsuranceObligation::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Оновлює загальну вартість деривативу та дату оновлення.
     */
    private void updateDerivative() {
        totalValue = obligations.stream()
                .mapToDouble(InsuranceObligation::calculateValue)
                .sum();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Фільтрує зобов'язання за текстовим пошуком та діапазоном розрахункової вартості.
     *
     * @param searchText текст для пошуку
     * @param minCalc мінімальна розрахункова вартість
     * @param maxCalc максимальна розрахункова вартість
     * @return відфільтрований список зобов'язань
     */
    public List<InsuranceObligation> filterObligations(String searchText, double minCalc, double maxCalc) {
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
     * @param sortOption критерій сортування
     * @return відсортований список зобов'язань
     */
    public List<InsuranceObligation> sortObligations(List<InsuranceObligation> obligations, String sortOption) {
        if (sortOption == null) return obligations;
        
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
     * @param searchText текст для пошуку
     * @param minCalcStr мінімальна розрахункова вартість (як строка)
     * @param maxCalcStr максимальна розрахункова вартість (як строка)
     * @param sortOption критерій сортування
     * @return відфільтрований та відсортований список зобов'язань
     */
    public List<InsuranceObligation> filterAndSortObligations(String searchText, String minCalcStr, String maxCalcStr, String sortOption) {
        try {
            double minCalc = minCalcStr.isEmpty() ? 0 : Double.parseDouble(minCalcStr);
            double maxCalc = maxCalcStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxCalcStr);

            List<InsuranceObligation> filtered = filterObligations(searchText.toLowerCase(), minCalc, maxCalc);
            return sortObligations(filtered, sortOption);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Derivative that = (Derivative) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Derivative{id=%d, name='%s', obligations=%d, totalValue=%.2f, created=%s, updated=%s}",
                id, name, obligations.size(), totalValue, createdAt, updatedAt);
    }
}