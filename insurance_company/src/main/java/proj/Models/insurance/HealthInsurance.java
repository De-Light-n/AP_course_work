package proj.Models.insurance;

import proj.Models.Risk;

/**
 * Клас HealthInsurance представляє страхування здоров'я.
 * Містить додаткові параметри, такі як вік, наявність хронічних хвороб,
 * ліміт покриття, включення госпіталізації та стоматології.
 * Додає стандартні ризики для медичного страхування.
 */
public class HealthInsurance extends InsuranceObligation {
    private int age;
    private boolean hasPreexistingConditions;
    private int coverageLimit;
    private boolean includesHospitalization;
    private boolean includesDentalCare;

    /**
     * Конструктор для повного створення HealthInsurance.
     *
     * @param riskLevel                рівень ризику (0-1)
     * @param amount                   сума страхування
     * @param durationMonths           тривалість у місяцях
     * @param age                      вік застрахованого
     * @param hasPreexistingConditions чи є хронічні хвороби
     * @param coverageLimit            ліміт покриття
     * @param includesHospitalization  чи включає госпіталізацію
     * @param includesDentalCare       чи включає стоматологію
     */
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

    /**
     * Конструктор для створення HealthInsurance з мінімальними параметрами.
     *
     * @param riskLevel      рівень ризику (0-1)
     * @param amount         сума страхування
     * @param durationMonths тривалість у місяцях
     */
    public HealthInsurance(double riskLevel, double amount, int durationMonths) {
        super(riskLevel, amount, durationMonths);
        setType("HEALTH");
        this.age = 0;
        this.hasPreexistingConditions = false;
        this.coverageLimit = 0;
        this.includesHospitalization = false;
        this.includesDentalCare = false;
    }

    /**
     * Конструктор копіювання з InsuranceObligation.
     *
     * @param other інший об'єкт InsuranceObligation
     */
    public HealthInsurance(InsuranceObligation other) {
        super(other);
        setType("HEALTH");
        this.age = 0;
        this.hasPreexistingConditions = false;
        this.coverageLimit = 0;
        this.includesHospitalization = false;
        this.includesDentalCare = false;
    }

    /**
     * Обчислює вартість страхування з урахуванням параметрів та ризиків.
     *
     * @return розрахована вартість
     */
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

    /**
     * Валідація віку.
     *
     * @param age вік
     * @return валідний вік
     * @throws IllegalArgumentException якщо вік некоректний
     */
    private int validateAge(int age) {
        if (age <= 0) {
            throw new IllegalArgumentException("Age must be positive");
        }
        return age;
    }

    // Гетери та сетери

    /**
     * @return вік застрахованого
     */
    public int getAge() {
        return age;
    }

    /**
     * @return чи є хронічні хвороби
     */
    public boolean hasPreexistingConditions() {
        return hasPreexistingConditions;
    }

    /**
     * @return ліміт покриття
     */
    public int getCoverageLimit() {
        return coverageLimit;
    }

    /**
     * @return чи включає госпіталізацію
     */
    public boolean includesHospitalization() {
        return includesHospitalization;
    }

    /**
     * @return чи включає стоматологію
     */
    public boolean includesDentalCare() {
        return includesDentalCare;
    }

    /**
     * Встановлює вік.
     *
     * @param age вік
     */
    public void setAge(int age) {
        this.age = validateAge(age);
    }

    /**
     * Встановлює наявність хронічних хвороб.
     *
     * @param hasPreexistingConditions чи є хронічні хвороби
     */
    public void setHasPreexistingConditions(boolean hasPreexistingConditions) {
        this.hasPreexistingConditions = hasPreexistingConditions;
    }

    /**
     * Встановлює ліміт покриття.
     *
     * @param coverageLimit ліміт покриття
     */
    public void setCoverageLimit(int coverageLimit) {
        this.coverageLimit = coverageLimit;
    }

    /**
     * Встановлює включення госпіталізації.
     *
     * @param includesHospitalization чи включає госпіталізацію
     */
    public void setIncludesHospitalization(boolean includesHospitalization) {
        this.includesHospitalization = includesHospitalization;
    }

    /**
     * Встановлює включення стоматології.
     *
     * @param includesDentalCare чи включає стоматологію
     */
    public void setIncludesDentalCare(boolean includesDentalCare) {
        this.includesDentalCare = includesDentalCare;
    }
}