package proj.Models;

import java.util.Objects;

/**
 * Клас Risk представляє страховий ризик.
 * Містить код, назву, опис, базовий коефіцієнт ризику та категорію ризику.
 */
public class Risk {
    private String code;
    private String name;
    private String description;
    private double baseRiskFactor;
    private RiskCategory category;

    /**
     * Перелік категорій ризиків.
     */
    public enum RiskCategory {
        PROPERTY, HEALTH, LIFE, LIABILITY, FINANCIAL
    }

    /**
     * Конструктор для створення ризику.
     *
     * @param code           код ризику
     * @param name           назва ризику
     * @param description    опис ризику
     * @param baseRiskFactor базовий коефіцієнт ризику
     * @param category       категорія ризику
     */
    public Risk(String code, String name, String description,
            double baseRiskFactor, RiskCategory category) {
        this.code = validateCode(code);
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.baseRiskFactor = validateRiskFactor(baseRiskFactor);
        this.category = Objects.requireNonNull(category);
    }

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

    /**
     * Валідує код ризику.
     *
     * @param code код ризику
     * @return валідний код
     * @throws IllegalArgumentException якщо код некоректний
     */
    private String validateCode(String code) {
        if (code == null || code.length() < 2) {
            throw new IllegalArgumentException("Risk code must be at least 2 character long");
        }
        return code.toUpperCase();
    }

    /**
     * Валідує коефіцієнт ризику.
     *
     * @param factor коефіцієнт ризику
     * @return валідний коефіцієнт
     * @throws IllegalArgumentException якщо коефіцієнт некоректний
     */
    private double validateRiskFactor(double factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Risk factor must be positive");
        }
        return factor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
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
