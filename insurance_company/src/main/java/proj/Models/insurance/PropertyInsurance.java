package proj.Models.insurance;

import java.util.List;
import java.util.Objects;

import proj.Models.Risk;

public class PropertyInsurance extends InsuranceObligation {
    public static final List<String> VALID_PROPERTY_TYPES = List.of("APARTMENT", "HOUSE", "COMMERCIAL");

    private String propertyLocation;
    private double propertyValue;
    private boolean isHighRiskArea;
    private String propertyType;

    private boolean includesNaturalDisasters;

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

        // Додавання стандартних ризиків
        this.addRisk(new Risk("FIRE01", "Пожежа", "Ризик пожежі", 0.15, Risk.RiskCategory.PROPERTY));
        this.addRisk(new Risk("THFT01", "Крадіжка", "Ризик крадіжки", 0.1, Risk.RiskCategory.PROPERTY));

        if (includesNaturalDisasters) {
            this.addRisk(new Risk("NATD01", "Стихія", "Ризик стихійного лиха", 0.2, Risk.RiskCategory.PROPERTY));
        }
    }

    public PropertyInsurance(double riskLevel, double amount, int durationMonths) {
        super(riskLevel, amount, durationMonths);
        setType("PROPERTY");
        this.propertyLocation = null;
        this.propertyValue = 0.0;
        this.isHighRiskArea = false;
        this.propertyType = null;
        this.includesNaturalDisasters = false;
    }

    public PropertyInsurance(InsuranceObligation other) {
        super(other);
        setType("PROPERTY");
        this.propertyLocation = null;
        this.propertyValue = 0.0;
        this.isHighRiskArea = false;
        this.propertyType = null;
        this.includesNaturalDisasters = false;
    }

    @Override
    public double calculateValue() {
        double baseValue = getAmount() * (1 + getRiskLevel() * 0.03);

        if (isHighRiskArea) {
            baseValue *= 1.5;
        }

        switch (propertyType) {
            case "HOUSE":
                baseValue *= 1.3;
                break;
            case "COMMERCIAL":
                baseValue *= 1.7;
                break;
        }

        if (includesNaturalDisasters) {
            baseValue *= 1.4;
        }

        // Врахування ризиків
        double riskFactor = getCoveredRisks().stream()
                .mapToDouble(Risk::getBaseRiskFactor)
                .sum();
        baseValue *= (1 + riskFactor);

        setCalculatedValue(baseValue);
        return baseValue;
    }

    private double validatePropertyValue(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Property value must be positive");
        }
        return value;
    }

    private String validatePropertyType(String type) {
        if (!VALID_PROPERTY_TYPES.contains(type)) {
            throw new IllegalArgumentException("Invalid property type");
        }
        return type;
    }

    // Гетери та сетери
    public String getPropertyLocation() {
        return propertyLocation;
    }

    public double getPropertyValue() {
        return propertyValue;
    }

    public boolean isHighRiskArea() {
        return isHighRiskArea;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public boolean includesNaturalDisasters() {
        return includesNaturalDisasters;
    }

    public void setPropertyLocation(String propertyLocation) {
        this.propertyLocation = Objects.requireNonNull(propertyLocation);
    }

    public void setPropertyValue(double propertyValue) {
        this.propertyValue = validatePropertyValue(propertyValue);
    }

    public void setHighRiskArea(boolean isHighRiskArea) {
        this.isHighRiskArea = isHighRiskArea;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = validatePropertyType(propertyType);
    }

    public void setIncludesNaturalDisasters(boolean includesNaturalDisasters) {
        this.includesNaturalDisasters = includesNaturalDisasters;
    }
}