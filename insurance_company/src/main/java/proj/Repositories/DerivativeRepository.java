package proj.Repositories;

import proj.Models.Derivative;
import proj.Models.insurance.InsuranceObligation;
import proj.Database.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторій для роботи з деривативами у базі даних.
 * Забезпечує CRUD-операції, пошук, збереження та видалення деривативів,
 * а також завантаження та оновлення пов'язаних страхових зобов'язань.
 */
public class DerivativeRepository {
    private final DatabaseManager dbManager;
    private InsuranceObligationRepository obligationRepository;

    /**
     * Створює новий репозиторій деривативів.
     */
    public DerivativeRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }

    private InsuranceObligationRepository getObligationRepository() {
        if (obligationRepository == null) {
            obligationRepository = new InsuranceObligationRepository();
        }
        return obligationRepository;
    }

    /**
     * Зберігає дериватив у базі даних.
     * Якщо дериватив новий — створює, інакше оновлює.
     *
     * @param derivative дериватив
     * @return збережений дериватив
     * @throws SQLException у разі помилки БД
     */
    public Derivative save(Derivative derivative) throws SQLException {
        if (derivative.getId() == 0) {
            return insert(derivative);
        } else {
            return update(derivative);
        }
    }

    private Derivative insert(Derivative derivative) throws SQLException {
        String sql = "INSERT INTO derivatives (name, total_value, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, derivative.getName());
            stmt.setDouble(2, derivative.getTotalValue());
            stmt.setTimestamp(3, Timestamp.valueOf(derivative.getCreatedAt()));
            stmt.setTimestamp(4, Timestamp.valueOf(derivative.getUpdatedAt()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    derivative.setId(rs.getInt("id"));
                    saveObligations(derivative);
                    return derivative;
                }
            }
        }
        throw new SQLException("Failed to insert derivative");
    }

    private Derivative update(Derivative derivative) throws SQLException {
        String sql = "UPDATE derivatives SET name = ?, total_value = ?, updated_at = ? " +
                "WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, derivative.getName());
            stmt.setDouble(2, derivative.getTotalValue());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(4, derivative.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                updateObligations(derivative);
                return derivative;
            }
        }
        throw new SQLException("Failed to update derivative");
    }

    /**
     * Повертає дериватив за ідентифікатором.
     *
     * @param id ідентифікатор деривативу
     * @return Optional з деривативом, якщо знайдено
     * @throws SQLException у разі помилки БД
     */
    public Optional<Derivative> findById(int id) throws SQLException {
        String sql = "SELECT * FROM derivatives WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Derivative derivative = mapDerivativeFromResultSet(rs);
                    loadObligations(derivative);
                    return Optional.of(derivative);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Повертає всі деривативи з бази даних.
     *
     * @return список деривативів
     * @throws SQLException у разі помилки БД
     */
    public List<Derivative> findAll() throws SQLException {
        String sql = "SELECT * FROM derivatives";
        List<Derivative> derivatives = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Derivative derivative = mapDerivativeFromResultSet(rs);
                loadObligations(derivative);
                derivatives.add(derivative);
            }
        }
        return derivatives;
    }

    /**
     * Видаляє дериватив за ідентифікатором.
     *
     * @param id ідентифікатор деривативу
     * @return true, якщо видалено
     * @throws SQLException у разі помилки БД
     */
    public boolean delete(int id) throws SQLException {
        String deleteRelationsSql = "DELETE FROM derivative_obligations WHERE derivative_id = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(deleteRelationsSql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }

        String sql = "DELETE FROM derivatives WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Derivative mapDerivativeFromResultSet(ResultSet rs) throws SQLException {
        Derivative derivative = new Derivative(rs.getString("name"));
        derivative.setId(rs.getInt("id"));
        derivative.setTotalValue(rs.getDouble("total_value"));
        derivative.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        derivative.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return derivative;
    }

    private void saveObligations(Derivative derivative) throws SQLException {
        String sql = "INSERT INTO derivative_obligations (derivative_id, obligation_id) VALUES (?, ?)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (InsuranceObligation obligation : derivative.getObligations()) {
                if (obligation.getId() == 0) {
                    getObligationRepository().save(obligation);
                }
                stmt.setInt(1, derivative.getId());
                stmt.setInt(2, obligation.getId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void updateObligations(Derivative derivative) throws SQLException {
        String deleteSql = "DELETE FROM derivative_obligations WHERE derivative_id = ?";
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, derivative.getId());
            stmt.executeUpdate();
        }
        saveObligations(derivative);
    }

    private void loadObligations(Derivative derivative) throws SQLException {
        String sql = "SELECT o.* FROM insurance_obligations o " +
                "JOIN derivative_obligations deo ON o.id = deo.obligation_id " +
                "WHERE deo.derivative_id = ?";

        List<InsuranceObligation> obligations = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, derivative.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Optional<InsuranceObligation> obligation = getObligationRepository().findById(rs.getInt("id"));
                    obligation.ifPresent(obligations::add);
                }
            }
        }
        derivative.setObligations(obligations);
    }

    /**
     * Повертає список деривативів за частковою назвою.
     *
     * @param name частина назви
     * @return список деривативів
     * @throws SQLException у разі помилки БД
     */
    public List<Derivative> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM derivatives WHERE name LIKE ?";
        List<Derivative> derivatives = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + name + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Derivative derivative = mapDerivativeFromResultSet(rs);
                    loadObligations(derivative);
                    derivatives.add(derivative);
                }
            }
        }
        return derivatives;
    }

    /**
     * Повертає список деривативів у заданому діапазоні загальної вартості.
     *
     * @param minValue мінімальна вартість
     * @param maxValue максимальна вартість
     * @return список деривативів
     * @throws SQLException у разі помилки БД
     */
    public List<Derivative> findByTotalValueRange(double minValue, double maxValue) throws SQLException {
        String sql = "SELECT * FROM derivatives WHERE total_value BETWEEN ? AND ?";
        List<Derivative> derivatives = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, minValue);
            stmt.setDouble(2, maxValue);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Derivative derivative = mapDerivativeFromResultSet(rs);
                    loadObligations(derivative);
                    derivatives.add(derivative);
                }
            }
        }
        return derivatives;
    }
}