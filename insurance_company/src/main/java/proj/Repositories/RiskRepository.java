package proj.Repositories;

import proj.Models.Risk;
import proj.Database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторій для роботи з ризиками у базі даних.
 * Забезпечує CRUD-операції, пошук, збереження, видалення ризиків,
 * а також пошук за категорією, фактором ризику, ідентифікатором зобов'язання
 * тощо.
 */
public class RiskRepository {
    private final DatabaseManager dbManager;

    public RiskRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Зберігає ризик у базі даних.
     * Якщо ризик існує — оновлює, інакше створює новий.
     *
     * @param risk ризик
     * @return збережений ризик
     * @throws SQLException у разі помилки БД
     */
    public Risk save(Risk risk) throws SQLException {
        if (risk.getCode() == null) {
            throw new IllegalArgumentException("Risk code cannot be null");
        }
        if (existsByCode(risk.getCode())) {
            return update(risk);
        } else {
            return insert(risk);
        }
    }

    /**
     * Ініціалізує стандартні ризики у системі, якщо їх ще немає.
     *
     * @throws SQLException у разі помилки БД
     */
    public void initializeStandardRisks() throws SQLException {
        List<Risk> standardRisks = List.of(
                new Risk("HLTH01", "Медичні витрати", "Ризик медичних витрат", 0.2, Risk.RiskCategory.HEALTH),
                new Risk("HOSP01", "Госпіталізація", "Ризик госпіталізації", 0.15, Risk.RiskCategory.HEALTH),
                new Risk("DENT01", "Стоматологія", "Ризик стоматологічних витрат", 0.1, Risk.RiskCategory.HEALTH),
                new Risk("DEATH01", "Смерть", "Ризик смерті застрахованої особи", 0.25, Risk.RiskCategory.LIFE),
                new Risk("CRIL01", "Критичне захворювання", "Ризик критичного захворювання", 0.15,
                        Risk.RiskCategory.LIFE),
                new Risk("ACCD01", "Нещасний випадок", "Ризик смерті від нещасного випадку", 0.10,
                        Risk.RiskCategory.LIFE),
                new Risk("FIRE01", "Пожежа", "Ризик пожежі", 0.15, Risk.RiskCategory.PROPERTY),
                new Risk("THFT01", "Крадіжка", "Ризик крадіжки", 0.1, Risk.RiskCategory.PROPERTY),
                new Risk("NATD01", "Стихія", "Ризик стихійного лиха", 0.2, Risk.RiskCategory.PROPERTY));
        for (Risk risk : standardRisks) {
            if (!existsByCode(risk.getCode())) {
                insert(risk);
            }
        }
    }

    private Risk insert(Risk risk) throws SQLException {
        String sql = "INSERT INTO risks (code, name, description, base_risk_factor, category) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            setRiskParameters(stmt, risk);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return risk;
            }
        }
        throw new SQLException("Failed to insert risk");
    }

    private Risk update(Risk risk) throws SQLException {
        String sql = "UPDATE risks SET name = ?, description = ?, base_risk_factor = ?, category = ? WHERE code = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, risk.getName());
            stmt.setString(2, risk.getDescription());
            stmt.setDouble(3, risk.getBaseRiskFactor());
            stmt.setString(4, risk.getCategory().toString());
            stmt.setString(5, risk.getCode());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                return risk;
            }
        }
        throw new SQLException("Failed to update risk");
    }

    /**
     * Повертає ризик за кодом.
     *
     * @param code код ризику
     * @return Optional з ризиком, якщо знайдено
     * @throws SQLException у разі помилки БД
     */
    public Optional<Risk> findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM risks WHERE code = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRiskFromResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Повертає всі ризики з бази даних.
     *
     * @return список ризиків
     * @throws SQLException у разі помилки БД
     */
    public List<Risk> findAll() throws SQLException {
        String sql = "SELECT * FROM risks";
        List<Risk> risks = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                risks.add(mapRiskFromResultSet(rs));
            }
        }
        return risks;
    }

    /**
     * Видаляє ризик за кодом.
     *
     * @param code код ризику
     * @return true, якщо видалено
     * @throws SQLException у разі помилки БД
     */
    public boolean delete(String code) throws SQLException {
        String sql = "DELETE FROM risks WHERE code = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Повертає список ризиків за категорією.
     *
     * @param category категорія ризику
     * @return список ризиків
     * @throws SQLException у разі помилки БД
     */
    public List<Risk> findByCategory(Risk.RiskCategory category) throws SQLException {
        String sql = "SELECT * FROM risks WHERE category = ?";
        List<Risk> risks = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    risks.add(mapRiskFromResultSet(rs));
                }
            }
        }
        return risks;
    }

    /**
     * Перевіряє, чи існує ризик за кодом.
     *
     * @param code код ризику
     * @return true, якщо існує
     * @throws SQLException у разі помилки БД
     */
    public boolean existsByCode(String code) throws SQLException {
        String sql = "SELECT 1 FROM risks WHERE code = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void setRiskParameters(PreparedStatement stmt, Risk risk) throws SQLException {
        stmt.setString(1, risk.getCode());
        stmt.setString(2, risk.getName());
        stmt.setString(3, risk.getDescription());
        stmt.setDouble(4, risk.getBaseRiskFactor());
        stmt.setString(5, risk.getCategory().toString());
    }

    private Risk mapRiskFromResultSet(ResultSet rs) throws SQLException {
        return new Risk(
                rs.getString("code"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("base_risk_factor"),
                Risk.RiskCategory.valueOf(rs.getString("category")));
    }

    /**
     * Повертає список ризиків за шаблоном назви.
     *
     * @param pattern шаблон назви
     * @return список ризиків
     * @throws SQLException у разі помилки БД
     */
    public List<Risk> findByNamePattern(String pattern) throws SQLException {
        String sql = "SELECT * FROM risks WHERE name LIKE ?";
        List<Risk> risks = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + pattern + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    risks.add(mapRiskFromResultSet(rs));
                }
            }
        }
        return risks;
    }

    /**
     * Повертає список ризиків у заданому діапазоні коефіцієнта ризику.
     *
     * @param minFactor мінімальний коефіцієнт
     * @param maxFactor максимальний коефіцієнт
     * @return список ризиків
     * @throws SQLException у разі помилки БД
     */
    public List<Risk> findByRiskFactorRange(double minFactor, double maxFactor) throws SQLException {
        String sql = "SELECT * FROM risks WHERE base_risk_factor BETWEEN ? AND ?";
        List<Risk> risks = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, minFactor);
            stmt.setDouble(2, maxFactor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    risks.add(mapRiskFromResultSet(rs));
                }
            }
        }
        return risks;
    }

    /**
     * Повертає список ризиків, пов'язаних із зобов'язанням за його ідентифікатором.
     *
     * @param obligationId ідентифікатор зобов'язання
     * @return список ризиків
     * @throws SQLException у разі помилки БД
     */
    public List<Risk> findByObligationId(int obligationId) throws SQLException {
        String sql = "SELECT r.* FROM risks r " +
                "JOIN obligation_risks \"or\" ON r.code = \"or\".risk_code " +
                "WHERE \"or\".obligation_id = ?";
        List<Risk> risks = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, obligationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    risks.add(mapRiskFromResultSet(rs));
                }
            }
        }
        return risks;
    }
}