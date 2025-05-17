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