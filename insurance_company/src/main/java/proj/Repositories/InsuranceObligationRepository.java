package proj.Repositories;

import proj.Models.insurance.HealthInsurance;
import proj.Models.insurance.InsuranceObligation;
import proj.Models.insurance.InsuranceObligation.ObligationStatus;
import proj.Models.insurance.LifeInsurance;
import proj.Models.insurance.PropertyInsurance;
import proj.Models.Risk;
import proj.Database.DatabaseManager;
import proj.Models.Derivative;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

/**
 * Репозиторій для роботи зі страховими зобов'язаннями у базі даних.
 * Забезпечує CRUD-операції, пошук, збереження, видалення, а також роботу з
 * ризиками та специфічними типами зобов'язань.
 */
public class InsuranceObligationRepository {
    private static final Logger logger = LogManager.getLogger(InsuranceObligationRepository.class);

    private final DatabaseManager dbManager;
    private DerivativeRepository derivativeRepository;

    public InsuranceObligationRepository() {
        this.dbManager = DatabaseManager.getInstance();
        logger.info("InsuranceObligationRepository ініціалізовано");
    }

    private DerivativeRepository getDerivativeRepository() {
        if (derivativeRepository == null) {
            derivativeRepository = new DerivativeRepository();
            logger.debug("DerivativeRepository ініціалізовано");
        }
        return derivativeRepository;
    }

    /**
     * Зберігає зобов'язання у базі даних.
     * Якщо зобов'язання нове — створює, інакше оновлює.
     *
     * @param obligation зобов'язання
     * @return збережене зобов'язання
     * @throws SQLException у разі помилки БД
     */
    public InsuranceObligation save(InsuranceObligation obligation) throws SQLException {
        logger.info("Збереження зобов'язання: {}", obligation.getPolicyNumber());
        if (obligation.getId() == 0) {
            return insert(obligation);
        } else {
            return update(obligation);
        }
    }

    /**
     * Зберігає зобов'язання та додає його до деривативу.
     *
     * @param obligation зобов'язання
     * @param derivative дериватив
     * @return збережене зобов'язання
     * @throws SQLException у разі помилки БД
     */
    public InsuranceObligation save(InsuranceObligation obligation, Derivative derivative) throws SQLException {
        logger.info("Збереження зобов'язання {} для деривативи {}", obligation.getPolicyNumber(), derivative.getName());
        InsuranceObligation savedObligation = save(obligation);
        if (derivative != null) {
            derivative.addObligation(savedObligation);
            getDerivativeRepository().save(derivative);
        }
        return savedObligation;
    }

    private InsuranceObligation insert(InsuranceObligation obligation) throws SQLException {
        logger.debug("Вставка нового зобов'язання: {}", obligation.getPolicyNumber());
        String sql = "INSERT INTO insurance_obligations (policy_number, type, risk_level, amount, " +
                "duration_months, calculated_value, start_date, end_date, status, notes, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            setObligationParameters(stmt, obligation);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    obligation.setId(rs.getInt("id"));
                    saveRisks(obligation);
                    saveSpecificTypeData(obligation);
                    logger.info("Зобов'язання успішно вставлено з ID: {}", obligation.getId());
                    return obligation;
                }
            }
        }
        logger.error("Не вдалося вставити зобов'язання: {}", obligation.getPolicyNumber());
        throw new SQLException("Failed to insert insurance obligation");
    }

    private InsuranceObligation update(InsuranceObligation obligation) throws SQLException {
        logger.debug("Оновлення зобов'язання з ID: {}", obligation.getId());
        String sql = "UPDATE insurance_obligations SET policy_number = ?, type = ?, risk_level = ?, " +
                "amount = ?, duration_months = ?, calculated_value = ?, start_date = ?, " +
                "end_date = ?, status = ?, notes = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            setObligationParameters(stmt, obligation);
            stmt.setInt(12, obligation.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                updateRisks(obligation);
                updateSpecificTypeData(obligation);
                logger.info("Зобов'язання з ID {} успішно оновлено", obligation.getId());
                return obligation;
            }
        }
        logger.error("Не вдалося оновити зобов'язання з ID: {}", obligation.getId());
        throw new SQLException("Failed to update insurance obligation");
    }

    /**
     * Повертає зобов'язання за ідентифікатором.
     *
     * @param id ідентифікатор зобов'язання
     * @return Optional з об'єктом зобов'язання, якщо знайдено
     * @throws SQLException у разі помилки БД
     */
    public Optional<InsuranceObligation> findById(int id) throws SQLException {
        logger.debug("Пошук зобов'язання за ID: {}", id);
        String sql = "SELECT * FROM insurance_obligations WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    InsuranceObligation obligation = mapObligationFromResultSet(rs);
                    loadRisks(obligation);
                    loadSpecificTypeData(obligation);
                    logger.info("Зобов'язання з ID {} знайдено", id);
                    return Optional.of(obligation);
                }
            }
        }
        logger.warn("Зобов'язання з ID {} не знайдено", id);
        return Optional.empty();
    }

    /**
     * Повертає всі зобов'язання з бази даних.
     *
     * @return список зобов'язань
     * @throws SQLException у разі помилки БД
     */
    public List<InsuranceObligation> findAll() throws SQLException {
        String sql = "SELECT * FROM insurance_obligations";
        List<InsuranceObligation> obligations = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                InsuranceObligation obligation = mapObligationFromResultSet(rs);
                loadRisks(obligation);
                loadSpecificTypeData(obligation);
                obligations.add(obligation);
            }
        }
        return obligations;
    }

    /**
     * Видаляє зобов'язання за ідентифікатором.
     *
     * @param id ідентифікатор зобов'язання
     * @return true, якщо видалено
     * @throws SQLException у разі помилки БД
     */
    public boolean delete(int id) throws SQLException {
        logger.debug("Видалення зобов'язання з ID: {}", id);
        String sql = "DELETE FROM insurance_obligations WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            boolean deleted = stmt.executeUpdate() > 0;
            if (deleted) {
                logger.info("Зобов'язання з ID {} успішно видалено", id);
            } else {
                logger.warn("Зобов'язання з ID {} не вдалося видалити", id);
            }
            return deleted;
        }
    }

    /**
     * Повертає список зобов'язань за статусом.
     *
     * @param status статус зобов'язання
     * @return список зобов'язань
     * @throws SQLException у разі помилки БД
     */
    public List<InsuranceObligation> findByStatus(ObligationStatus status) throws SQLException {
        String sql = "SELECT * FROM insurance_obligations WHERE status = ?";
        List<InsuranceObligation> obligations = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InsuranceObligation obligation = mapObligationFromResultSet(rs);
                    loadRisks(obligation);
                    loadSpecificTypeData(obligation);
                    obligations.add(obligation);
                }
            }
        }
        return obligations;
    }

    private void setObligationParameters(PreparedStatement stmt, InsuranceObligation obligation)
            throws SQLException {
        stmt.setString(1, obligation.getPolicyNumber());
        stmt.setString(2, obligation.getType().toUpperCase());
        stmt.setDouble(3, obligation.getRiskLevel());
        stmt.setDouble(4, obligation.getAmount());
        stmt.setInt(5, obligation.getDurationMonths());
        stmt.setDouble(6, obligation.getCalculatedValue());
        stmt.setTimestamp(7, Timestamp.valueOf(obligation.getStartDate()));
        stmt.setTimestamp(8, Timestamp.valueOf(obligation.getEndDate()));
        stmt.setString(9, obligation.getStatus().toString());
        stmt.setString(10, obligation.getNotes());
        stmt.setTimestamp(11, Timestamp.valueOf(obligation.getCreatedAt()));
        stmt.setTimestamp(12, Timestamp.valueOf(obligation.getUpdatedAt()));
    }

    private InsuranceObligation mapObligationFromResultSet(ResultSet rs) throws SQLException {
        InsuranceObligation obligation = new InsuranceObligation(
                rs.getDouble("risk_level"),
                rs.getDouble("amount"),
                rs.getInt("duration_months")) {
            @Override
            public double calculateValue() {
                return getCalculatedValue();
            }
        };

        obligation.setId(rs.getInt("id"));
        obligation.setPolicyNumber(rs.getString("policy_number"));
        obligation.setType(rs.getString("type"));
        obligation.setCalculatedValue(rs.getDouble("calculated_value"));
        obligation.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
        obligation.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
        obligation.setStatus(ObligationStatus.valueOf(rs.getString("status")));
        obligation.setNotes(rs.getString("notes"));
        obligation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        obligation.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        switch (obligation.getType()) {
            case "LIFE":
                obligation = new LifeInsurance(obligation);
                break;
            case "HEALTH":
                obligation = new HealthInsurance(obligation);
                break;
            case "PROPERTY":
                obligation = new PropertyInsurance(obligation);
                break;
            default:
                throw new SQLException("Unknown insurance type: " + obligation.getType());
        }
        loadSpecificTypeData(obligation);

        return obligation;
    }

    private void saveRisks(InsuranceObligation obligation) throws SQLException {
        logger.debug("Збереження ризиків для зобов'язання з ID: {}", obligation.getId());
        String sql = "INSERT INTO obligation_risks (obligation_id, risk_code) VALUES (?, ?)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Risk risk : obligation.getCoveredRisks()) {
                stmt.setInt(1, obligation.getId());
                stmt.setString(2, risk.getCode());
                stmt.addBatch();
            }
            stmt.executeBatch();
            logger.info("Ризики для зобов'язання з ID {} успішно збережено", obligation.getId());
        }
    }

    private void updateRisks(InsuranceObligation obligation) throws SQLException {
        String deleteSql = "DELETE FROM obligation_risks WHERE obligation_id = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, obligation.getId());
            stmt.executeUpdate();
        }
        saveRisks(obligation);
    }

    private void loadRisks(InsuranceObligation obligation) throws SQLException {
        logger.debug("Завантаження ризиків для зобов'язання з ID: {}", obligation.getId());
        String sql = "SELECT r.* FROM risks r " +
                "JOIN obligation_risks obr ON r.code = obr.risk_code " +
                "WHERE obr.obligation_id = ?";

        Set<Risk> risks = new HashSet<>();
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, obligation.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    risks.add(new Risk(
                            rs.getString("code"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getDouble("base_risk_factor"),
                            Risk.RiskCategory.valueOf(rs.getString("category"))));
                }
            }
        }
        obligation.getCoveredRisks().clear();
        obligation.getCoveredRisks().addAll(risks);
        logger.info("Ризики для зобов'язання з ID {} успішно завантажено", obligation.getId());
    }

    private void saveSpecificTypeData(InsuranceObligation obligation) throws SQLException {
        if (obligation instanceof LifeInsurance) {
            saveLifeInsuranceData((LifeInsurance) obligation);
        } else if (obligation instanceof HealthInsurance) {
            saveHealthInsuranceData((HealthInsurance) obligation);
        } else if (obligation instanceof PropertyInsurance) {
            savePropertyInsuranceData((PropertyInsurance) obligation);
        }
    }

    private void updateSpecificTypeData(InsuranceObligation obligation) throws SQLException {
        if (obligation instanceof LifeInsurance) {
            updateLifeInsuranceData((LifeInsurance) obligation);
        } else if (obligation instanceof HealthInsurance) {
            updateHealthInsuranceData((HealthInsurance) obligation);
        } else if (obligation instanceof PropertyInsurance) {
            updatePropertyInsuranceData((PropertyInsurance) obligation);
        }
    }

    private void loadSpecificTypeData(InsuranceObligation obligation) throws SQLException {
        if (obligation == null)
            return;

        String type = obligation.getType();
        switch (type) {
            case "LIFE":
                loadLifeInsuranceData(obligation);
                break;
            case "HEALTH":
                loadHealthInsuranceData(obligation);
                break;
            case "PROPERTY":
                loadPropertyInsuranceData(obligation);
                break;
        }
    }

    private void saveLifeInsuranceData(LifeInsurance lifeInsurance) throws SQLException {
        String sql = "INSERT INTO life_insurance (obligation_id, beneficiary, " +
                "includes_critical_illness, includes_accidental_death) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, lifeInsurance.getId());
            stmt.setString(2, lifeInsurance.getBeneficiary());
            stmt.setBoolean(3, lifeInsurance.includesCriticalIllness());
            stmt.setBoolean(4, lifeInsurance.includesAccidentalDeath());

            stmt.executeUpdate();
        }
    }

    private void updateLifeInsuranceData(LifeInsurance lifeInsurance) throws SQLException {
        String sql = "UPDATE life_insurance SET beneficiary = ?, " +
                "includes_critical_illness = ?, includes_accidental_death = ? " +
                "WHERE obligation_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, lifeInsurance.getBeneficiary());
            stmt.setBoolean(2, lifeInsurance.includesCriticalIllness());
            stmt.setBoolean(3, lifeInsurance.includesAccidentalDeath());
            stmt.setInt(4, lifeInsurance.getId());

            stmt.executeUpdate();
        }
    }

    private void loadLifeInsuranceData(InsuranceObligation obligation) throws SQLException {
        String sql = "SELECT * FROM life_insurance WHERE obligation_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, obligation.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && obligation instanceof LifeInsurance) {
                    LifeInsurance lifeInsurance = (LifeInsurance) obligation;
                    lifeInsurance.setBeneficiary(rs.getString("beneficiary"));
                    lifeInsurance.setIncludesCriticalIllness(rs.getBoolean("includes_critical_illness"));
                    lifeInsurance.setIncludesAccidentalDeath(rs.getBoolean("includes_accidental_death"));
                }
            }
        }
    }

    private void saveHealthInsuranceData(HealthInsurance healthInsurance) throws SQLException {
        String sql = "INSERT INTO health_insurance (obligation_id, age, has_preexisting_conditions, " +
                "coverage_limit, includes_hospitalization, includes_dental_care) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, healthInsurance.getId());
            stmt.setInt(2, healthInsurance.getAge());
            stmt.setBoolean(3, healthInsurance.hasPreexistingConditions());
            stmt.setInt(4, healthInsurance.getCoverageLimit());
            stmt.setBoolean(5, healthInsurance.includesHospitalization());
            stmt.setBoolean(6, healthInsurance.includesDentalCare());

            stmt.executeUpdate();
        }
    }

    private void updateHealthInsuranceData(HealthInsurance healthInsurance) throws SQLException {
        String sql = "UPDATE health_insurance SET age = ?, has_preexisting_conditions = ?, " +
                "coverage_limit = ?, includes_hospitalization = ?, includes_dental_care = ? " +
                "WHERE obligation_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, healthInsurance.getAge());
            stmt.setBoolean(2, healthInsurance.hasPreexistingConditions());
            stmt.setInt(3, healthInsurance.getCoverageLimit());
            stmt.setBoolean(4, healthInsurance.includesHospitalization());
            stmt.setBoolean(5, healthInsurance.includesDentalCare());
            stmt.setInt(6, healthInsurance.getId());

            stmt.executeUpdate();
        }
    }

    private void loadHealthInsuranceData(InsuranceObligation obligation) throws SQLException {
        String sql = "SELECT * FROM health_insurance WHERE obligation_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, obligation.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && obligation instanceof HealthInsurance) {
                    HealthInsurance healthInsurance = (HealthInsurance) obligation;
                    healthInsurance.setAge(rs.getInt("age"));
                    healthInsurance.setHasPreexistingConditions(rs.getBoolean("has_preexisting_conditions"));
                    healthInsurance.setCoverageLimit(rs.getInt("coverage_limit"));
                    healthInsurance.setIncludesHospitalization(rs.getBoolean("includes_hospitalization"));
                    healthInsurance.setIncludesDentalCare(rs.getBoolean("includes_dental_care"));
                }
            }
        }
    }

    private void savePropertyInsuranceData(PropertyInsurance propertyInsurance) throws SQLException {
        String sql = "INSERT INTO property_insurance (obligation_id, property_location, " +
                "property_value, is_high_risk_area, property_type, includes_natural_disasters) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, propertyInsurance.getId());
            stmt.setString(2, propertyInsurance.getPropertyLocation());
            stmt.setDouble(3, propertyInsurance.getPropertyValue());
            stmt.setBoolean(4, propertyInsurance.isHighRiskArea());
            stmt.setString(5, propertyInsurance.getPropertyType());
            stmt.setBoolean(6, propertyInsurance.includesNaturalDisasters());

            stmt.executeUpdate();
        }
    }

    private void updatePropertyInsuranceData(PropertyInsurance propertyInsurance) throws SQLException {
        String sql = "UPDATE property_insurance SET property_location = ?, property_value = ?, " +
                "is_high_risk_area = ?, property_type = ?, includes_natural_disasters = ? " +
                "WHERE obligation_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, propertyInsurance.getPropertyLocation());
            stmt.setDouble(2, propertyInsurance.getPropertyValue());
            stmt.setBoolean(3, propertyInsurance.isHighRiskArea());
            stmt.setString(4, propertyInsurance.getPropertyType());
            stmt.setBoolean(5, propertyInsurance.includesNaturalDisasters());
            stmt.setInt(6, propertyInsurance.getId());

            stmt.executeUpdate();
        }
    }

    private void loadPropertyInsuranceData(InsuranceObligation obligation) throws SQLException {
        String sql = "SELECT * FROM property_insurance WHERE obligation_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, obligation.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && obligation instanceof PropertyInsurance) {
                    PropertyInsurance propertyInsurance = (PropertyInsurance) obligation;
                    propertyInsurance.setPropertyLocation(rs.getString("property_location"));
                    propertyInsurance.setPropertyValue(rs.getDouble("property_value"));
                    propertyInsurance.setHighRiskArea(rs.getBoolean("is_high_risk_area"));
                    propertyInsurance.setPropertyType(rs.getString("property_type"));
                    propertyInsurance.setIncludesNaturalDisasters(rs.getBoolean("includes_natural_disasters"));
                }
            }
        }
    }
}