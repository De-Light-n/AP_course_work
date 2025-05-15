package proj.Models.insurance;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import proj.Models.Risk;

public abstract class InsuranceObligation {
    private int id;
    private String policyNumber;
    private String type;
    private double riskLevel;
    private double amount;
    private int durationMonths;
    private double calculatedValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ObligationStatus status;
    private String notes;
    private Set<Risk> coveredRisks = new HashSet<>();

    public enum ObligationStatus {
        DRAFT, ACTIVE, EXPIRED, CANCELLED, PENDING, CLAIMED
    }

    public InsuranceObligation(double riskLevel, double amount, int durationMonths) {
        this.riskLevel = validateRiskLevel(riskLevel);
        this.amount = validateAmount(amount);
        this.durationMonths = validateDuration(durationMonths);
        this.startDate = LocalDateTime.now();
        this.endDate = startDate.plusMonths(durationMonths);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = ObligationStatus.DRAFT;
        this.policyNumber = generatePolicyNumber();
    }

    public InsuranceObligation(InsuranceObligation other) {
        this.id = other.id;
        this.policyNumber = other.policyNumber;
        this.type = other.type;
        this.riskLevel = other.riskLevel;
        this.amount = other.amount;
        this.durationMonths = other.durationMonths;
        this.calculatedValue = other.calculatedValue;
        this.startDate = other.startDate;
        this.endDate = other.endDate;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
        this.status = other.status;
        this.notes = other.notes;
    }

    // Гетери та сетери
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = Objects.requireNonNull(policyNumber);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.updatedAt = LocalDateTime.now(); // Оновлюємо час останньої зміни
    }

    public double getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(double riskLevel) {
        this.riskLevel = validateRiskLevel(riskLevel);
        this.updatedAt = LocalDateTime.now();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = validateAmount(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(int durationMonths) {
        this.durationMonths = validateDuration(durationMonths);
        this.endDate = startDate.plusMonths(durationMonths);
        this.updatedAt = LocalDateTime.now();
    }

    public double getCalculatedValue() {
        return calculatedValue;
    }

    public void setCalculatedValue(double calculatedValue) {
        this.calculatedValue = calculatedValue;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = startDate.plusMonths(durationMonths);
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = Objects.requireNonNull(endDate);
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public ObligationStatus getStatus() {
        return status;
    }

    public void setStatus(ObligationStatus status) {
        this.status = Objects.requireNonNull(status);
        this.updatedAt = LocalDateTime.now();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Set<Risk> getCoveredRisks() {
        return new HashSet<>(coveredRisks);
    }

    public void setRisks(Set<Risk> risks) {
        this.coveredRisks = new HashSet<>(Objects.requireNonNull(risks));
        this.updatedAt = LocalDateTime.now(); // Оновлюємо час останньої зміни
    }

    // Методи для роботи з ризиками
    public void addRisk(Risk risk) {
        coveredRisks.add(Objects.requireNonNull(risk));
        this.updatedAt = LocalDateTime.now();
    }

    public void removeRisk(Risk risk) {
        coveredRisks.remove(Objects.requireNonNull(risk));
        this.updatedAt = LocalDateTime.now();
    }

    public boolean coversRisk(String riskCode) {
        return coveredRisks.stream()
                .anyMatch(r -> r.getCode().equals(riskCode));
    }

    // Бізнес-методи
    public void activate() {
        if (status == ObligationStatus.DRAFT) {
            status = ObligationStatus.ACTIVE;
            startDate = LocalDateTime.now();
            endDate = startDate.plusMonths(durationMonths);
            updatedAt = LocalDateTime.now();
        }
    }

    public void cancel() {
        if (status == ObligationStatus.ACTIVE || status == ObligationStatus.PENDING) {
            status = ObligationStatus.CANCELLED;
            updatedAt = LocalDateTime.now();
        }
    }

    public void renew(int additionalMonths) {
        if (isActive()) {
            durationMonths += additionalMonths;
            endDate = endDate.plusMonths(additionalMonths);
            updatedAt = LocalDateTime.now();
        }
    }

    public boolean isActive() {
        return status == ObligationStatus.ACTIVE &&
                LocalDateTime.now().isBefore(endDate);
    }

    public double calculatePremiumPerMonth() {
        return calculateValue() / durationMonths;
    }

    // Валідація
    private double validateRiskLevel(double riskLevel) {
        if (riskLevel < 0 || riskLevel > 1) {
            throw new IllegalArgumentException("Risk level must be between 0 and 1");
        }
        return riskLevel;
    }

    private double validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return amount;
    }

    private int validateDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        return duration;
    }

    private String generatePolicyNumber() {
        return "POL-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }

    // Абстрактний метод для розрахунку вартості
    public abstract double calculateValue();

    /**
     * Дозволяє безпечно привести до потрібного підкласу або кинути виняток.
     * 
     * @param <T>   тип підкласу
     * @param clazz клас підкласу
     * @return this, приведений до підкласу
     * @throws ClassCastException якщо об'єкт не є екземпляром clazz
     */
    public <T extends InsuranceObligation> T as(Class<T> clazz) {
        if (clazz.isInstance(this)) {
            return clazz.cast(this);
        }
        throw new ClassCastException("Cannot cast " + getClass().getSimpleName() + " to " + clazz.getSimpleName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InsuranceObligation that = (InsuranceObligation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d, policyNumber='%s', status=%s, risk=%.2f, amount=%.2f, duration=%d months}",
                getClass().getSimpleName(), id, policyNumber, status, riskLevel, amount, durationMonths);
    }
}