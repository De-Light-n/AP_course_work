package proj.Models.insurance;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import proj.Models.Risk;

/**
 * Абстрактний клас, що представляє страхове зобов'язання.
 * Містить основні поля, такі як сума, тривалість, статус, ризики, дати та інше.
 * Всі конкретні типи страхових зобов'язань повинні наслідувати цей клас
 * та реалізовувати метод {@link #calculateValue()}.
 */
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

    /**
     * Перелік можливих статусів страхового зобов'язання.
     */
    public enum ObligationStatus {
        DRAFT, ACTIVE, EXPIRED, CANCELLED, PENDING, CLAIMED
    }

    /**
     * Конструктор для створення страхового зобов'язання з основними параметрами.
     *
     * @param riskLevel      рівень ризику (0-1)
     * @param amount         сума страхування
     * @param durationMonths тривалість у місяцях
     */
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

    /**
     * Конструктор копіювання.
     *
     * @param other інший об'єкт InsuranceObligation
     */
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

    /**
     * @return ідентифікатор зобов'язання
     */
    public int getId() {
        return id;
    }

    /**
     * Встановлює ідентифікатор зобов'язання.
     *
     * @param id ідентифікатор
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return номер поліса
     */
    public String getPolicyNumber() {
        return policyNumber;
    }

    /**
     * Встановлює номер поліса.
     *
     * @param policyNumber номер поліса
     */
    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = Objects.requireNonNull(policyNumber);
    }

    /**
     * @return тип зобов'язання
     */
    public String getType() {
        return type;
    }

    /**
     * Встановлює тип зобов'язання.
     *
     * @param type тип зобов'язання
     */
    public void setType(String type) {
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * @return рівень ризику
     */
    public double getRiskLevel() {
        return riskLevel;
    }

    /**
     * Встановлює рівень ризику.
     *
     * @param riskLevel рівень ризику (0-1)
     */
    public void setRiskLevel(double riskLevel) {
        this.riskLevel = validateRiskLevel(riskLevel);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * @return сума страхування
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Встановлює суму страхування.
     *
     * @param amount сума страхування
     */
    public void setAmount(double amount) {
        this.amount = validateAmount(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * @return тривалість у місяцях
     */
    public int getDurationMonths() {
        return durationMonths;
    }

    /**
     * Встановлює тривалість у місяцях.
     *
     * @param durationMonths тривалість у місяцях
     */
    public void setDurationMonths(int durationMonths) {
        this.durationMonths = validateDuration(durationMonths);
        this.endDate = startDate.plusMonths(durationMonths);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * @return розрахована вартість
     */
    public double getCalculatedValue() {
        return calculatedValue;
    }

    /**
     * Встановлює розраховану вартість.
     *
     * @param calculatedValue розрахована вартість
     */
    public void setCalculatedValue(double calculatedValue) {
        this.calculatedValue = calculatedValue;
    }

    /**
     * @return дата початку дії зобов'язання
     */
    public LocalDateTime getStartDate() {
        return startDate;
    }

    /**
     * Встановлює дату початку дії зобов'язання.
     *
     * @param startDate дата початку
     */
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = startDate.plusMonths(durationMonths);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * @return дата завершення дії зобов'язання
     */
    public LocalDateTime getEndDate() {
        return endDate;
    }

    /**
     * Встановлює дату завершення дії зобов'язання.
     *
     * @param endDate дата завершення
     */
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = Objects.requireNonNull(endDate);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * @return дата створення зобов'язання
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Встановлює дату створення зобов'язання.
     *
     * @param createdAt дата створення
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    /**
     * @return дата останнього оновлення
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Встановлює дату останнього оновлення.
     *
     * @param updatedAt дата оновлення
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    /**
     * @return статус зобов'язання
     */
    public ObligationStatus getStatus() {
        return status;
    }

    /**
     * Встановлює статус зобов'язання.
     *
     * @param status статус
     */
    public void setStatus(ObligationStatus status) {
        this.status = Objects.requireNonNull(status);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * @return нотатки до зобов'язання
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Встановлює нотатки до зобов'язання.
     *
     * @param notes нотатки
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return набір покритих ризиків
     */
    public Set<Risk> getCoveredRisks() {
        return coveredRisks;
    }

    /**
     * Встановлює набір покритих ризиків.
     *
     * @param risks набір ризиків
     */
    public void setRisks(Set<Risk> risks) {
        this.coveredRisks = new HashSet<>(Objects.requireNonNull(risks));
        this.updatedAt = LocalDateTime.now();
    }

    // Методи для роботи з ризиками

    /**
     * Додає ризик до покриття.
     *
     * @param risk ризик
     */
    public void addRisk(Risk risk) {
        coveredRisks.add(Objects.requireNonNull(risk));
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Видаляє ризик з покриття.
     *
     * @param risk ризик
     */
    public void removeRisk(Risk risk) {
        coveredRisks.remove(Objects.requireNonNull(risk));
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Перевіряє, чи покривається ризик за кодом.
     *
     * @param riskCode код ризику
     * @return true, якщо ризик покривається
     */
    public boolean coversRisk(String riskCode) {
        return coveredRisks.stream()
                .anyMatch(r -> r.getCode().equals(riskCode));
    }

    // Бізнес-методи

    /**
     * Активує зобов'язання, якщо воно у статусі DRAFT.
     * Оновлює дати початку та завершення.
     */
    public void activate() {
        if (status == ObligationStatus.DRAFT) {
            status = ObligationStatus.ACTIVE;
            startDate = LocalDateTime.now();
            endDate = startDate.plusMonths(durationMonths);
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Скасовує зобов'язання, якщо воно активне або очікує.
     */
    public void cancel() {
        if (status == ObligationStatus.ACTIVE || status == ObligationStatus.PENDING) {
            status = ObligationStatus.CANCELLED;
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Продовжує дію зобов'язання на додаткову кількість місяців, якщо воно активне.
     *
     * @param additionalMonths кількість додаткових місяців
     */
    public void renew(int additionalMonths) {
        if (isActive()) {
            durationMonths += additionalMonths;
            endDate = endDate.plusMonths(additionalMonths);
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Перевіряє, чи є зобов'язання активним на поточний момент.
     *
     * @return true, якщо активне
     */
    public boolean isActive() {
        return status == ObligationStatus.ACTIVE &&
                LocalDateTime.now().isBefore(endDate);
    }

    /**
     * Обчислює щомісячний платіж за зобов'язанням.
     *
     * @return щомісячний платіж
     */
    public double calculatePremiumPerMonth() {
        return calculateValue() / durationMonths;
    }

    // Валідація

    /**
     * Валідує рівень ризику.
     *
     * @param riskLevel рівень ризику
     * @return валідний рівень ризику
     * @throws IllegalArgumentException якщо рівень ризику некоректний
     */
    private double validateRiskLevel(double riskLevel) {
        if (riskLevel < 0 || riskLevel > 1) {
            throw new IllegalArgumentException("Risk level must be between 0 and 1");
        }
        return riskLevel;
    }

    /**
     * Валідує суму страхування.
     *
     * @param amount сума
     * @return валідна сума
     * @throws IllegalArgumentException якщо сума некоректна
     */
    private double validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return amount;
    }

    /**
     * Валідує тривалість.
     *
     * @param duration тривалість у місяцях
     * @return валідна тривалість
     * @throws IllegalArgumentException якщо тривалість некоректна
     */
    private int validateDuration(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        return duration;
    }

    /**
     * Генерує унікальний номер поліса.
     *
     * @return номер поліса
     */
    private String generatePolicyNumber() {
        return "POL-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
    }

    /**
     * Абстрактний метод для розрахунку вартості зобов'язання.
     *
     * @return розрахована вартість
     */
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