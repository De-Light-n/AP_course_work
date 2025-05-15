package proj.Models;

import java.util.Objects;

public class Risk {
    private String code;
    private String name;
    private String description;
    private double baseRiskFactor;
    private RiskCategory category;

    public enum RiskCategory {
        PROPERTY, HEALTH, LIFE, LIABILITY, FINANCIAL
    }

    public Risk(String code, String name, String description, 
               double baseRiskFactor, RiskCategory category) {
        this.code = validateCode(code);
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.baseRiskFactor = validateRiskFactor(baseRiskFactor);
        this.category = Objects.requireNonNull(category);
    }

    // Гетери та сетери
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBaseRiskFactor() {
        return baseRiskFactor;
    }

    public void setBaseRiskFactor(double baseRiskFactor) {
        this.baseRiskFactor = validateRiskFactor(baseRiskFactor);
    }

    public RiskCategory getCategory() {
        return category;
    }

    public void setCategory(RiskCategory category) {
        this.category = Objects.requireNonNull(category);
    }

    // Валідація
    private String validateCode(String code) {
        if (code == null || code.length() < 2) {
            throw new IllegalArgumentException("Risk code must be at least 2 character long");
        }
        return code.toUpperCase();
    }

    private double validateRiskFactor(double factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Risk factor must be positive");
        }
        return factor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Risk risk = (Risk) o;
        return code.equals(risk.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return String.format("Risk{code='%s', name='%s', category=%s}", code, name, category);
    }
}
