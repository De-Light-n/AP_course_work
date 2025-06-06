package proj.Models.insurance;

import java.util.Objects;
import proj.Models.Risk;

/**
 * Модель страхування життя.
 * Містить параметри: вигодонабувач, включення критичних захворювань та нещасних
 * випадків.
 * Додає стандартні ризики для страхування життя.
 */
public class LifeInsurance extends InsuranceObligation {
    private String beneficiary;
    private boolean includesCriticalIllness;
    private boolean includesAccidentalDeath;

    /**
     * Повний конструктор для створення страхування життя.
     *
     * @param riskLevel               рівень ризику (0-1)
     * @param amount                  сума страхування
     * @param durationMonths          тривалість у місяцях
     * @param beneficiary             вигодонабувач
     * @param includesCriticalIllness чи включає критичні захворювання
     * @param includesAccidentalDeath чи включає нещасний випадок
     */
    public LifeInsurance(double riskLevel, double amount, int durationMonths,
            String beneficiary, boolean includesCriticalIllness,
            boolean includesAccidentalDeath) {
        super(riskLevel, amount, durationMonths);
        setType("LIFE");
        this.beneficiary = Objects.requireNonNull(beneficiary);
        this.includesCriticalIllness = includesCriticalIllness;
        this.includesAccidentalDeath = includesAccidentalDeath;

        this.addRisk(new Risk("DEATH01", "Смерть", "Ризик смерті застрахованої особи", 0.25, Risk.RiskCategory.LIFE));
        if (includesCriticalIllness) {
            this.addRisk(new Risk("CRIL01", "Критичне захворювання", "Ризик критичного захворювання", 0.15,
                    Risk.RiskCategory.LIFE));
        }
        if (includesAccidentalDeath) {
            this.addRisk(new Risk("ACCD01", "Нещасний випадок", "Ризик смерті від нещасного випадку", 0.10,
                    Risk.RiskCategory.LIFE));
        }
    }

    /**
     * Конструктор для створення страхування життя з мінімальними параметрами.
     *
     * @param riskLevel      рівень ризику (0-1)
     * @param amount         сума страхування
     * @param durationMonths тривалість у місяцях
     */
    public LifeInsurance(double riskLevel, double amount, int durationMonths) {
        super(riskLevel, amount, durationMonths);
        setType("LIFE");
        this.beneficiary = null;
        this.includesCriticalIllness = false;
        this.includesAccidentalDeath = false;
    }

    /**
     * Конструктор копіювання з InsuranceObligation.
     *
     * @param other інший об'єкт InsuranceObligation
     */
    public LifeInsurance(InsuranceObligation other) {
        super(other);
        setType("LIFE");
        this.beneficiary = null;
        this.includesCriticalIllness = false;
        this.includesAccidentalDeath = false;
    }

    /**
     * Повертає вигодонабувача.
     *
     * @return вигодонабувач
     */
    public String getBeneficiary() {
        return beneficiary;
    }

    /**
     * Встановлює вигодонабувача.
     *
     * @param beneficiary вигодонабувач
     */
    public void setBeneficiary(String beneficiary) {
        this.beneficiary = Objects.requireNonNull(beneficiary);
    }

    /**
     * Чи включає критичні захворювання.
     *
     * @return true, якщо включає критичні захворювання
     */
    public boolean includesCriticalIllness() {
        return includesCriticalIllness;
    }

    /**
     * Чи включає нещасний випадок.
     *
     * @return true, якщо включає нещасний випадок
     */
    public boolean includesAccidentalDeath() {
        return includesAccidentalDeath;
    }

    /**
     * Встановлює включення критичних захворювань.
     *
     * @param includesCriticalIllness чи включає критичні захворювання
     */
    public void setIncludesCriticalIllness(boolean includesCriticalIllness) {
        this.includesCriticalIllness = includesCriticalIllness;
    }

    /**
     * Встановлює включення нещасного випадку.
     *
     * @param includesAccidentalDeath чи включає нещасний випадок
     */
    public void setIncludesAccidentalDeath(boolean includesAccidentalDeath) {
        this.includesAccidentalDeath = includesAccidentalDeath;
    }
}