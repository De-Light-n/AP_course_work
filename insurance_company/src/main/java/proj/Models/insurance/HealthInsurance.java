package proj.Models.insurance;

import proj.Models.Risk;

/**
 * Модель страхування здоров'я.
 * Містить параметри: вік, наявність хронічних хвороб, ліміт покриття,
 * включення госпіталізації та стоматології. Додає стандартні ризики для
 * медичного страхування.
 */
public class HealthInsurance extends InsuranceObligation {
    private int age;
    private boolean hasPreexistingConditions;
    private int coverageLimit;
    private boolean includesHospitalization;
    private boolean includesDentalCare;

    /**
     * Повний конструктор для створення страхування здоров'я.
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
     * Конструктор для створення страхування здоров'я з мінімальними параметрами.
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
     * Валідація віку.
     *
     * @param age вік
     * @return валідний вік
     * @throws IllegalArgumentException якщо вік не позитивний
     */
    private int validateAge(int age) {
        if (age <= 0) {
            throw new IllegalArgumentException("Вік має бути додатнім");
        }
        return age;
    }

    /**
     * Повертає вік застрахованого.
     *
     * @return вік
     */
    public int getAge() {
        return age;
    }

    /**
     * Чи є хронічні хвороби.
     *
     * @return true, якщо є хронічні хвороби
     */
    public boolean hasPreexistingConditions() {
        return hasPreexistingConditions;
    }

    /**
     * Повертає ліміт покриття.
     *
     * @return ліміт покриття
     */
    public int getCoverageLimit() {
        return coverageLimit;
    }

    /**
     * Чи включає госпіталізацію.
     *
     * @return true, якщо включає госпіталізацію
     */
    public boolean includesHospitalization() {
        return includesHospitalization;
    }

    /**
     * Чи включає стоматологію.
     *
     * @return true, якщо включає стоматологію
     */
    public boolean includesDentalCare() {
        return includesDentalCare;
    }

    /**
     * Встановлює вік застрахованого.
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