package Fintech.servicies;

import Fintech.entities.User;
import Fintech.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser implements IService<User> {

    private Connection connection;

    public ServiceUser() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    @Override
    public void ajouter(User user) throws SQLException {
        String sql = "INSERT INTO user (name, email, phone, password, role, face_image) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, user.getName());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPhone());
        ps.setString(4, user.getPassword());
        ps.setString(5, user.getRole());
        ps.setString(6, user.getFaceImage());

        ps.executeUpdate();
    }

    @Override
    public void modifier(User user) throws SQLException {
        String sql = "UPDATE user SET name = ?, email = ?, phone = ?, password = ?, role = ?, face_image = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, user.getName());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPhone());
        ps.setString(4, user.getPassword());
        ps.setString(5, user.getRole());
        ps.setString(6, user.getFaceImage());
        ps.setInt(7, user.getId());

        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<User> afficher() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        while (rs.next()) {
            User u = mapUser(rs);
            users.add(u);
        }
        return users;
    }

    public boolean validateLogin(String email, String password) {
        String sql = "SELECT * FROM user WHERE email = ? AND password = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User getUserByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM user WHERE email = ? AND password = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Finds a user by email only (used for Google Sign-In).
     */
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a new Google user if not existing, or returns the existing one.
     * Google users get role "user", empty phone and password.
     */
    public User createOrUpdateGoogleUser(String name, String email) {
        // Case-insensitive email lookup to avoid duplicate account issues
        User existing = getUserByEmailIgnoreCase(email);
        if (existing != null) {
            return existing;
        }
        String sql = "INSERT INTO user (name, email, phone, password, role, face_image) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name != null ? name : email);
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, "");
            ps.setString(4, "google_oauth");
            ps.setString(5, "user");
            ps.setString(6, ""); // face_image is NOT NULL — use empty string for Google users
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                User user = new User();
                user.setId(keys.getInt(1));
                user.setName(name != null ? name : email);
                user.setEmail(email.trim().toLowerCase());
                user.setPhone("");
                user.setPassword("google_oauth");
                user.setRole("user");
                user.setFaceImage(null);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("[Google OAuth] SQL Error: " + e.getMessage() + " | SQLState: " + e.getSQLState());
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByEmailIgnoreCase(String email) {
        String sql = "SELECT * FROM user WHERE LOWER(email) = LOWER(?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, email.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Records the current timestamp as last_login for the given user.
     * Call this right after a successful login.
     */
    public void updateLastLogin(int userId) {
        String sql = "UPDATE user SET last_login = NOW() WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Internal helper: maps a ResultSet row to a User object,
     * including the new last_login and created_at columns (safe even if columns are
     * missing).
     */
    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setFaceImage(rs.getString("face_image"));
        try {
            user.setLastLogin(rs.getTimestamp("last_login"));
        } catch (SQLException ignored) {
        }
        try {
            user.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException ignored) {
        }
        return user;
    }

    // ──────────────────────────── MOT DE PASSE OUBLIÉ ────────────────────────────

    /**
     * Vérifie si un email existe dans la table user.
     */
    public boolean emailExists(String email) {
        String sql = "SELECT id FROM user WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sauvegarde le code de réinitialisation dans la colonne reset_code.
     */
    public void saveResetCode(String email, String code) throws SQLException {
        String sql = "UPDATE user SET reset_code = ? WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    /**
     * Vérifie le code de réinitialisation sans modifier le mot de passe.
     *
     * @return true si le code correspond au code stocké en base
     */
    public boolean verifyResetCode(String email, String code) {
        String sql = "SELECT reset_code FROM user WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedCode = rs.getString("reset_code");
                return storedCode != null && storedCode.equals(code);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Met à jour le mot de passe et efface le reset_code en base.
     *
     * @return true si la mise à jour a réussi
     */
    public boolean updatePasswordAndClearCode(String email, String newPassword) {
        String sql = "UPDATE user SET password = ?, reset_code = NULL WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Vérifie le code de réinitialisation.
     * Si correct, met à jour le mot de passe et efface le code.
     *
     * @return true si le code correspond et le mot de passe a été mis à jour
     */
    public boolean verifyCodeAndUpdatePassword(String email, String code, String newPassword) {
        if (verifyResetCode(email, code)) {
            return updatePasswordAndClearCode(email, newPassword);
        }
        return false;
    }
}
