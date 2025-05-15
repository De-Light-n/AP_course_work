package proj.Models;

import proj.Models.insurance.InsuranceObligation;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

public class Derivative {
    private int id;
    private String name;
    private List<InsuranceObligation> obligations;
    private double totalValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Derivative(String name) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.obligations = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Гетери та сетери
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

    public List<InsuranceObligation> getObligations() {
        return Collections.unmodifiableList(obligations);
    }

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

    // Основні методи
    public void addObligation(InsuranceObligation obligation) {
        if (!obligations.contains(obligation)) {
            obligations.add(obligation);
            totalValue += obligation.calculateValue(); // Викликати calculateValue() замість getCalculatedValue()
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeObligation(InsuranceObligation obligation) {
        obligations.remove(obligation);
        updateDerivative();
    }

    public void sortByRiskLevel() {
        obligations.sort((o1, o2) -> Double.compare(o2.getRiskLevel(), o1.getRiskLevel()));
        this.updatedAt = LocalDateTime.now();
    }

    public List<InsuranceObligation> findObligationsInRange(double minRisk, double maxRisk,
            double minAmount, double maxAmount) {
        return obligations.stream()
                .filter(o -> o.getRiskLevel() >= minRisk && o.getRiskLevel() <= maxRisk)
                .filter(o -> o.getAmount() >= minAmount && o.getAmount() <= maxAmount)
                .collect(Collectors.toList());
    }

    public Map<Risk.RiskCategory, Long> countRisksByCategory() {
        return obligations.stream()
                .flatMap(o -> o.getCoveredRisks().stream())
                .collect(Collectors.groupingBy(
                        Risk::getCategory,
                        Collectors.counting()));
    }

    public double calculateAverageRisk() {
        return obligations.stream()
                .mapToDouble(InsuranceObligation::getRiskLevel)
                .average()
                .orElse(0.0);
    }

    public Map<String, Long> countObligationsByType() {
        return obligations.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getClass().getSimpleName(),
                        Collectors.counting()));
    }

    public List<InsuranceObligation> getActiveObligations() {
        return obligations.stream()
                .filter(InsuranceObligation::isActive)
                .collect(Collectors.toList());
    }

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