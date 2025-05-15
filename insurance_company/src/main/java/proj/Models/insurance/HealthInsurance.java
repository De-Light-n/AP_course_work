package proj.Models.insurance;

import proj.Models.Risk;

public class HealthInsurance extends InsuranceObligation {
    private int age;
    private boolean hasPreexistingConditions;
    private int coverageLimit;
    private boolean includesHospitalization;
    private boolean includesDentalCare;

    public HealthInsurance(double riskLevel, double amount, int durationMonths,
            int age, boolean hasPreexistingConditions,
            int coverageLimit, boolean includesHospitalization,
            boolean includesDentalCare) {
        super(riskLevel, amount, durationMonths);
        setType("HEALTH");
        this.age = validateAge(age);
        this.hasPreexistingConditions = hasPreexistingConditions;
        this.coverageLimit = coverageLimit;
        this.includesHospitalization = includesHospitalization;
        this.includesDentalCare = includesDentalCare;

        // Додавання стандартних ризиків
        this.addRisk(new Risk("HLTH01", "Медичні витрати", "Ризик медичних витрат", 0.2, Risk.RiskCategory.HEALTH));
        if (includesHospitalization) {
            this.addRisk(new Risk("HOSP01", "Госпіталізація", "Ризик госпіталізації", 0.15, Risk.RiskCategory.HEALTH));
        }
        if (includesDentalCare) {
            this.addRisk(
                    new Risk("DENT01", "Стоматологія", "Ризик стоматологічних витрат", 0.1, Risk.RiskCategory.HEALTH));
        }
    }

    public HealthInsurance(double riskLevel, double amount, int durationMonths) {
        super(riskLevel, amount, durationMonths);
        setType("HEALTH");
        this.age = 0;
        this.hasPreexistingConditions = false;
        this.coverageLimit = 0;
        this.includesHospitalization = false;
        this.includesDentalCare = false;
    }

    public HealthInsurance(InsuranceObligation other) {
        super(other);
        setType("HEALTH");
        this.age = 0;
        this.hasPreexistingConditions = false;
        this.coverageLimit = 0;
        this.includesHospitalization = false;
        this.includesDentalCare = false;
    }

    @Override
    public double calculateValue() {
        double baseValue = getAmount() * (1 + getRiskLevel() * 0.05);

        // Коефіцієнти за віком
        if (age > 50) {
            baseValue *= 1.5;
        } else if (age > 30) {
            baseValue *= 1.2;
        }

        if (hasPreexistingConditions) {
            baseValue *= 1.8;
        }

        if (includesHospitalization) {
            baseValue *= 1.3;
        }

        if (includesDentalCare) {
            baseValue *= 1.1;
        }

        // Врахування ризиків
        double riskFactor = getCoveredRisks().stream()
                .mapToDouble(Risk::getBaseRiskFactor)
                .sum();
        baseValue *= (1 + riskFactor);

        setCalculatedValue(baseValue);
        return baseValue;
    }

    private int validateAge(int age) {
        if (age <= 0) {
            throw new IllegalArgumentException("Age must be positive");
        }
        return age;
    }

    // Гетери та сетери
    public int getAge() {
        return age;
    }

    public boolean hasPreexistingConditions() {
        return hasPreexistingConditions;
    }

    public int getCoverageLimit() {
        return coverageLimit;
    }

    public boolean includesHospitalization() {
        return includesHospitalization;
    }

    public boolean includesDentalCare() {
        return includesDentalCare;
    }

    public void setAge(int age) {
        this.age = validateAge(age);
    }

    public void setHasPreexistingConditions(boolean hasPreexistingConditions) {
        this.hasPreexistingConditions = hasPreexistingConditions;
    }

    public void setCoverageLimit(int coverageLimit) {
        this.coverageLimit = coverageLimit;
    }

    public void setIncludesHospitalization(boolean includesHospitalization) {
        this.includesHospitalization = includesHospitalization;
    }

    public void setIncludesDentalCare(boolean includesDentalCare) {
        this.includesDentalCare = includesDentalCare;
    }
}