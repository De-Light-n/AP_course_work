package proj.Models.insurance;

import java.util.Objects;

import proj.Models.Risk;

public class LifeInsurance extends InsuranceObligation {
    private String beneficiary;
    private boolean includesCriticalIllness;
    private boolean includesAccidentalDeath;

    public LifeInsurance(double riskLevel, double amount, int durationMonths,
            String beneficiary, boolean includesCriticalIllness,
            boolean includesAccidentalDeath) {
        super(riskLevel, amount, durationMonths);
        setType("LIFE");
        this.beneficiary = Objects.requireNonNull(beneficiary);
        this.includesCriticalIllness = includesCriticalIllness;
        this.includesAccidentalDeath = includesAccidentalDeath;

        // Додавання стандартних ризиків для страхування життя
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

    public LifeInsurance(double riskLevel, double amount, int durationMonths) {
        super(riskLevel, amount, durationMonths);
        setType("LIFE");
        this.beneficiary = null;
        this.includesCriticalIllness = false;
        this.includesAccidentalDeath = false;
    }

    public LifeInsurance(InsuranceObligation other) {
        super(other);
        setType("LIFE");
        this.beneficiary = null;
        this.includesCriticalIllness = false;
        this.includesAccidentalDeath = false;
    }


    @Override
    public double calculateValue() {
        double baseValue = getAmount() * (1 + getRiskLevel() * 0.1);

        // Додаткові коефіцієнти для додаткових ризиків
        if (includesCriticalIllness) {
            baseValue *= 1.3;
        }
        if (includesAccidentalDeath) {
            baseValue *= 1.2;
        }

        // Врахування ризиків
        double riskFactor = getCoveredRisks().stream()
                .mapToDouble(Risk::getBaseRiskFactor)
                .sum();
        baseValue *= (1 + riskFactor);

        setCalculatedValue(baseValue);
        return baseValue;
    }

    // Гетери та сетери
    public String getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(String beneficiary) {
        this.beneficiary = Objects.requireNonNull(beneficiary);
    }

    public boolean includesCriticalIllness() {
        return includesCriticalIllness;
    }

    public boolean includesAccidentalDeath() {
        return includesAccidentalDeath;
    }

    public void setIncludesCriticalIllness(boolean includesCriticalIllness) {
        this.includesCriticalIllness = includesCriticalIllness;
    }

    public void setIncludesAccidentalDeath(boolean includesAccidentalDeath) {
        this.includesAccidentalDeath = includesAccidentalDeath;
    }
}