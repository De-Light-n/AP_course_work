package proj.Models.insurance;

import java.util.List;
import java.util.Objects;
import proj.Models.Risk;

/**
 * Модель страхування майна.
 * Містить параметри: місцезнаходження, вартість майна, тип майна,
 * чи знаходиться у зоні підвищеного ризику, та чи включає стихійні лиха.
 * Додає стандартні ризики для страхування майна.
 */
public class PropertyInsurance extends InsuranceObligation {
    /**
     * Дозволені типи майна.
     */
    public static final List<String> VALID_PROPERTY_TYPES = List.of("APARTMENT", "HOUSE", "COMMERCIAL");

    private String propertyLocation;
    private double propertyValue;
    private boolean isHighRiskArea;
    private String propertyType;
    private boolean includesNaturalDisasters;

    /**
     * Конструктор для створення страхування майна з усіма параметрами.
     *
     * @param riskLevel                рівень ризику (0-1)
     * @param amount                   сума страхування
     * @param durationMonths           тривалість у місяцях
     * @param propertyLocation         місцезнаходження майна
     * @param propertyValue            вартість майна
     * @param isHighRiskArea           чи знаходиться у зоні підвищеного ризику
     * @param propertyType             тип майна
     * @param includesNaturalDisasters чи включає стихійні лиха
     */
    public PropertyInsurance(double riskLevel, double amount, int durationMonths,
            String propertyLocation, double propertyValue,
            boolean isHighRiskArea, String propertyType,
            boolean includesNaturalDisasters) {
        super(riskLevel, amount, durationMonths);
        setType("PROPERTY");
        this.propertyLocation = Objects.requireNonNull(propertyLocation);
        this.propertyValue = validatePropertyValue(propertyValue);
        this.isHighRiskArea = isHighRiskArea;
        this.propertyType = validatePropertyType(propertyType);
        this.includesNaturalDisasters = includesNaturalDisasters;

        this.addRisk(new Risk("FIRE01", "Пожежа", "Ризик пожежі", 0.15, Risk.RiskCategory.PROPERTY));
        this.addRisk(new Risk("THFT01", "Крадіжка", "Ризик крадіжки", 0.1, Risk.RiskCategory.PROPERTY));
        if (includesNaturalDisasters) {
            this.addRisk(new Risk("NATD01", "Стихія", "Ризик стихійного лиха", 0.2, Risk.RiskCategory.PROPERTY));
        }
    }

    /**
     * Конструктор для створення страхування майна з мінімальними параметрами.
     *
     * @param riskLevel      рівень ризику (0-1)
     * @param amount         сума страхування
     * @param durationMonths тривалість у місяцях
     */
    public PropertyInsurance(double riskLevel, double amount, int durationMonths) {
        super(riskLevel, amount, durationMonths);
        setType("PROPERTY");
        this.propertyLocation = null;
        this.propertyValue = 0.0;
        this.isHighRiskArea = false;
        this.propertyType = null;
        this.includesNaturalDisasters = false;
    }

    /**
     * Конструктор копіювання з InsuranceObligation.
     *
     * @param other інший об'єкт InsuranceObligation
     */
    public PropertyInsurance(InsuranceObligation other) {
        super(other);
        setType("PROPERTY");
        this.propertyLocation = null;
        this.propertyValue = 0.0;
        this.isHighRiskArea = false;
        this.propertyType = null;
        this.includesNaturalDisasters = false;
    }

    /**
     * Валідує вартість майна.
     *
     * @param value вартість майна
     * @return валідна вартість
     * @throws IllegalArgumentException якщо значення від'ємне
     */
    private double validatePropertyValue(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Вартість майна має бути додатною");
        }
        return value;
    }

    /**
     * Валідує тип майна.
     *
     * @param type тип майна
     * @return валідний тип майна
     * @throws IllegalArgumentException якщо тип некоректний
     */
    private String validatePropertyType(String type) {
        if (!VALID_PROPERTY_TYPES.contains(type)) {
            throw new IllegalArgumentException("Некоректний тип майна");
        }
        return type;
    }

    /**
     * Повертає місцезнаходження майна.
     *
     * @return місцезнаходження майна
     */
    public String getPropertyLocation() {
        return propertyLocation;
    }

    /**
     * Повертає вартість майна.
     *
     * @return вартість майна
     */
    public double getPropertyValue() {
        return propertyValue;
    }

    /**
     * Чи знаходиться у зоні підвищеного ризику.
     *
     * @return true, якщо у зоні підвищеного ризику
     */
    public boolean isHighRiskArea() {
        return isHighRiskArea;
    }

    /**
     * Повертає тип майна.
     *
     * @return тип майна
     */
    public String getPropertyType() {
        return propertyType;
    }

    /**
     * Чи включає стихійні лиха.
     *
     * @return true, якщо включає стихійні лиха
     */
    public boolean includesNaturalDisasters() {
        return includesNaturalDisasters;
    }

    /**
     * Встановлює місцезнаходження майна.
     *
     * @param propertyLocation місцезнаходження майна
     */
    public void setPropertyLocation(String propertyLocation) {
        this.propertyLocation = Objects.requireNonNull(propertyLocation);
    }

    /**
     * Встановлює вартість майна.
     *
     * @param propertyValue вартість майна
     */
    public void setPropertyValue(double propertyValue) {
        this.propertyValue = validatePropertyValue(propertyValue);
    }

    /**
     * Встановлює, чи знаходиться у зоні підвищеного ризику.
     *
     * @param isHighRiskArea true, якщо у зоні підвищеного ризику
     */
    public void setHighRiskArea(boolean isHighRiskArea) {
        this.isHighRiskArea = isHighRiskArea;
    }

    /**
     * Встановлює тип майна.
     *
     * @param propertyType тип майна
     */
    public void setPropertyType(String propertyType) {
        this.propertyType = validatePropertyType(propertyType);
    }

    /**
     * Встановлює включення стихійних лих.
     *
     * @param includesNaturalDisasters чи включає стихійні лиха
     */
    public void setIncludesNaturalDisasters(boolean includesNaturalDisasters) {
        this.includesNaturalDisasters = includesNaturalDisasters;
    }
}