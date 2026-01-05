import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager.java - Database Operations
 * 
 * Handles all SQLite database operations for the Job Listing System.
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private Connection connection;
    
    // H2 Database Configuration
    // File-based: "jdbc:h2:./job_listing" - creates job_listing.mv.db file
    // AUTO_SERVER=TRUE allows multiple connections
    private static final String DB_URL = "jdbc:h2:./job_listing;AUTO_SERVER=TRUE";
    private static final String DB_USER = "sa";  // default H2 username
    private static final String DB_PASSWORD = ""; // default empty password
    
    private DatabaseManager() {
        connect();
        createTables();
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void connect() {
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✓ Connected to H2 database");
        } catch (ClassNotFoundException e) {
            System.err.println("H2 JDBC driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }
    
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    private void createTables() {
        if (!isConnected()) return;
        
        try (Statement stmt = connection.createStatement()) {
            // Users table (H2 syntax)
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(255) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255), " +
                "role VARCHAR(50) NOT NULL DEFAULT 'Customer')");
            
            // Add email column if it doesn't exist (for existing databases)
            try {
                // Check if email column exists
                ResultSet rs = stmt.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'USERS' AND COLUMN_NAME = 'EMAIL'");
                if (!rs.next()) {
                    // Column doesn't exist, add it
                    stmt.execute("ALTER TABLE users ADD COLUMN email VARCHAR(255)");
                }
                rs.close();
            } catch (SQLException e) {
                // Column might already exist or table doesn't exist yet, ignore
            }
            
            // Jobs table (H2 syntax)
            stmt.execute("CREATE TABLE IF NOT EXISTS jobs (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "company VARCHAR(255), " +
                "location VARCHAR(255), " +
                "salary VARCHAR(100), " +
                "description TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Applications table (H2 syntax)
            stmt.execute("CREATE TABLE IF NOT EXISTS applications (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(255) NOT NULL, " +
                "job_title VARCHAR(255) NOT NULL, " +
                "company VARCHAR(255), " +
                "applicant_name VARCHAR(255), " +
                "email VARCHAR(255), " +
                "phone VARCHAR(50), " +
                "cover_letter TEXT, " +
                "status VARCHAR(50) DEFAULT 'Pending', " +
                "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Notifications table (H2 syntax)
            stmt.execute("CREATE TABLE IF NOT EXISTS notifications (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(255) NOT NULL, " +
                "job_title VARCHAR(255) NOT NULL, " +
                "message TEXT, " +
                "status VARCHAR(50), " +
                "is_read INT DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Resumes table (H2 syntax)
            stmt.execute("CREATE TABLE IF NOT EXISTS resumes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(255) UNIQUE NOT NULL, " +
                "full_name VARCHAR(255), " +
                "email VARCHAR(255), " +
                "phone VARCHAR(50), " +
                "address TEXT, " +
                "education TEXT, " +
                "experience TEXT, " +
                "skills TEXT, " +
                "summary TEXT, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Contact Messages table (H2 syntax)
            stmt.execute("CREATE TABLE IF NOT EXISTS contact_messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(255) NOT NULL, " +
                "subject VARCHAR(255) NOT NULL, " +
                "message TEXT NOT NULL, " +
                "email VARCHAR(255), " +
                "phone VARCHAR(50), " +
                "status VARCHAR(50) DEFAULT 'New', " +
                "is_read INT DEFAULT 0, " +
                "admin_response TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // User Sessions table (H2 syntax) - tracks login/logout
            stmt.execute("CREATE TABLE IF NOT EXISTS user_sessions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(255) NOT NULL, " +
                "login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "logout_time TIMESTAMP, " +
                "is_active INT DEFAULT 1)");
            
            // Add default admin user if not exists (H2 MERGE syntax)
            PreparedStatement ps = connection.prepareStatement(
                "MERGE INTO users (username, password, role) KEY(username) VALUES (?, ?, ?)");
            ps.setString(1, "admin");
            ps.setString(2, "admin123");
            ps.setString(3, "Admin");
            ps.executeUpdate();
            ps.close();
            
            System.out.println("✓ Database tables created/verified");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }
    
    public void addSampleJobs() {
        if (!isConnected()) return;
        
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM jobs");
            if (rs.next() && rs.getInt(1) == 0) {
                String[] sampleJobs = {
                    "INSERT INTO jobs (title, company, location, salary, description) VALUES ('Software Engineer', 'Tech Corp', 'Manila', '80,000 PHP', 'Develop and maintain software applications')",
                    "INSERT INTO jobs (title, company, location, salary, description) VALUES ('Data Analyst', 'Data Inc', 'Makati', '60,000 PHP', 'Analyze business data and create reports')",
                    "INSERT INTO jobs (title, company, location, salary, description) VALUES ('Project Manager', 'Global Solutions', 'BGC', '100,000 PHP', 'Lead and manage project teams')",
                    "INSERT INTO jobs (title, company, location, salary, description) VALUES ('UI/UX Designer', 'Creative Agency', 'Cebu', '55,000 PHP', 'Design user interfaces and experiences')",
                    "INSERT INTO jobs (title, company, location, salary, description) VALUES ('Network Administrator', 'IT Services', 'Quezon City', '50,000 PHP', 'Manage and maintain network infrastructure')"
                };
                for (String sql : sampleJobs) {
                    stmt.executeUpdate(sql);
                }
                System.out.println("✓ Sample jobs added");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error adding sample jobs: " + e.getMessage());
        }
    }
    
    // ============== USER OPERATIONS ==============
    
    public boolean userExists(String username) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "SELECT COUNT(*) FROM users WHERE username = ?");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next() && rs.getInt(1) > 0;
        rs.close();
        ps.close();
        return exists;
    }
    
    public boolean addUser(String username, String password, String email, String role) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)");
        ps.setString(1, username);
        ps.setString(2, password);
        ps.setString(3, email);
        ps.setString(4, role);
        int result = ps.executeUpdate();
        ps.close();
        return result > 0;
    }
    
    public boolean deleteUser(int userId, String username) throws SQLException {
        // Prevent deleting admin user
        if ("admin".equalsIgnoreCase(username)) {
            throw new SQLException("Cannot delete the admin user.");
        }
        
        // Delete all related data first (in order to avoid foreign key issues)
        // 1. Delete user sessions
        PreparedStatement deleteSessions = connection.prepareStatement(
            "DELETE FROM user_sessions WHERE username = ?");
        deleteSessions.setString(1, username);
        deleteSessions.executeUpdate();
        deleteSessions.close();
        
        // 2. Delete applications
        PreparedStatement deleteApplications = connection.prepareStatement(
            "DELETE FROM applications WHERE username = ?");
        deleteApplications.setString(1, username);
        deleteApplications.executeUpdate();
        deleteApplications.close();
        
        // 3. Delete notifications
        PreparedStatement deleteNotifications = connection.prepareStatement(
            "DELETE FROM notifications WHERE username = ?");
        deleteNotifications.setString(1, username);
        deleteNotifications.executeUpdate();
        deleteNotifications.close();
        
        // 4. Delete resume
        PreparedStatement deleteResume = connection.prepareStatement(
            "DELETE FROM resumes WHERE username = ?");
        deleteResume.setString(1, username);
        deleteResume.executeUpdate();
        deleteResume.close();
        
        // 5. Delete contact messages
        PreparedStatement deleteContactMessages = connection.prepareStatement(
            "DELETE FROM contact_messages WHERE username = ?");
        deleteContactMessages.setString(1, username);
        deleteContactMessages.executeUpdate();
        deleteContactMessages.close();
        
        // 6. Finally, delete the user
        PreparedStatement ps = connection.prepareStatement(
            "DELETE FROM users WHERE id = ?");
        ps.setInt(1, userId);
        int result = ps.executeUpdate();
        ps.close();
        return result > 0;
    }
    
    public int deleteAllUsersExceptAdmin() throws SQLException {
        // Get all usernames except admin
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT username FROM users WHERE LOWER(username) != 'admin'");
        
        int deletedCount = 0;
        while (rs.next()) {
            String username = rs.getString("username");
            try {
                // Delete all related data for each user
                // 1. Delete user sessions
                PreparedStatement deleteSessions = connection.prepareStatement(
                    "DELETE FROM user_sessions WHERE username = ?");
                deleteSessions.setString(1, username);
                deleteSessions.executeUpdate();
                deleteSessions.close();
                
                // 2. Delete applications
                PreparedStatement deleteApplications = connection.prepareStatement(
                    "DELETE FROM applications WHERE username = ?");
                deleteApplications.setString(1, username);
                deleteApplications.executeUpdate();
                deleteApplications.close();
                
                // 3. Delete notifications
                PreparedStatement deleteNotifications = connection.prepareStatement(
                    "DELETE FROM notifications WHERE username = ?");
                deleteNotifications.setString(1, username);
                deleteNotifications.executeUpdate();
                deleteNotifications.close();
                
                // 4. Delete resume
                PreparedStatement deleteResume = connection.prepareStatement(
                    "DELETE FROM resumes WHERE username = ?");
                deleteResume.setString(1, username);
                deleteResume.executeUpdate();
                deleteResume.close();
                
                // 5. Delete contact messages
                PreparedStatement deleteContactMessages = connection.prepareStatement(
                    "DELETE FROM contact_messages WHERE username = ?");
                deleteContactMessages.setString(1, username);
                deleteContactMessages.executeUpdate();
                deleteContactMessages.close();
                
                // 6. Delete the user
                PreparedStatement deleteUser = connection.prepareStatement(
                    "DELETE FROM users WHERE username = ?");
                deleteUser.setString(1, username);
                deleteUser.executeUpdate();
                deleteUser.close();
                
                deletedCount++;
            } catch (SQLException e) {
                System.err.println("Error deleting user " + username + ": " + e.getMessage());
            }
        }
        rs.close();
        stmt.close();
        return deletedCount;
    }
    
    public String validateLogin(String username, String password) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "SELECT role FROM users WHERE username = ? AND password = ?");
        ps.setString(1, username);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();
        String role = null;
        if (rs.next()) {
            role = rs.getString("role");
            // Record login session
            recordLogin(username);
        }
        rs.close();
        ps.close();
        return role;
    }
    
    public void recordLogin(String username) throws SQLException {
        // Mark previous sessions as inactive
        PreparedStatement updatePs = connection.prepareStatement(
            "UPDATE user_sessions SET is_active = 0 WHERE username = ? AND is_active = 1");
        updatePs.setString(1, username);
        updatePs.executeUpdate();
        updatePs.close();
        
        // Create new active session
        PreparedStatement insertPs = connection.prepareStatement(
            "INSERT INTO user_sessions (username, is_active) VALUES (?, 1)");
        insertPs.setString(1, username);
        insertPs.executeUpdate();
        insertPs.close();
    }
    
    public void recordLogout(String username) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "UPDATE user_sessions SET logout_time = CURRENT_TIMESTAMP, is_active = 0 " +
            "WHERE username = ? AND is_active = 1");
        ps.setString(1, username);
        ps.executeUpdate();
        ps.close();
    }
    
    // ============== JOB OPERATIONS ==============
    
    public List<String[]> getAllJobs() throws SQLException {
        List<String[]> jobs = new ArrayList<>();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id, title, company, location, salary, description FROM jobs ORDER BY id DESC");
        while (rs.next()) {
            jobs.add(new String[]{
                String.valueOf(rs.getInt("id")),
                rs.getString("title"),
                rs.getString("company"),
                rs.getString("location"),
                rs.getString("salary"),
                rs.getString("description")
            });
        }
        rs.close();
        stmt.close();
        return jobs;
    }
    
    public int addJob(String title, String company, String location, String salary, String description) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO jobs (title, company, location, salary, description) VALUES (?, ?, ?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, title);
        ps.setString(2, company);
        ps.setString(3, location);
        ps.setString(4, salary);
        ps.setString(5, description);
        ps.executeUpdate();
        
        ResultSet rs = ps.getGeneratedKeys();
        int newId = rs.next() ? rs.getInt(1) : -1;
        rs.close();
        ps.close();
        return newId;
    }
    
    public boolean updateJob(int id, String title, String company, String location, String salary, String description) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "UPDATE jobs SET title = ?, company = ?, location = ?, salary = ?, description = ? WHERE id = ?");
        ps.setString(1, title);
        ps.setString(2, company);
        ps.setString(3, location);
        ps.setString(4, salary);
        ps.setString(5, description);
        ps.setInt(6, id);
        int result = ps.executeUpdate();
        ps.close();
        return result > 0;
    }
    
    public boolean deleteJob(int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("DELETE FROM jobs WHERE id = ?");
        ps.setInt(1, id);
        int result = ps.executeUpdate();
        ps.close();
        return result > 0;
    }
    
    // ============== APPLICATION OPERATIONS ==============
    
    public int getPendingApplicationCount() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM applications WHERE status = 'Pending'");
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close();
        stmt.close();
        return count;
    }
    
    public List<Object[]> getAllApplications() throws SQLException {
        List<Object[]> apps = new ArrayList<>();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT id, username, job_title, company, applicant_name, email, phone, cover_letter, status, applied_at " +
            "FROM applications ORDER BY applied_at DESC");
        while (rs.next()) {
            apps.add(new Object[]{
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("job_title"),
                rs.getString("company"),
                rs.getString("applicant_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("cover_letter"),
                rs.getString("status"),
                rs.getString("applied_at")
            });
        }
        rs.close();
        stmt.close();
        return apps;
    }
    
    public List<Object[]> getUserApplications(String username) throws SQLException {
        List<Object[]> apps = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(
            "SELECT id, job_title, company, applicant_name, email, status, applied_at " +
            "FROM applications WHERE username = ? ORDER BY applied_at DESC");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            apps.add(new Object[]{
                rs.getInt("id"),
                rs.getString("job_title"),
                rs.getString("company"),
                rs.getString("applicant_name"),
                rs.getString("email"),
                rs.getString("status"),
                rs.getString("applied_at")
            });
        }
        rs.close();
        ps.close();
        return apps;
    }
    
    public boolean addApplication(String username, String jobTitle, String company, 
                                  String name, String email, String phone, String coverLetter) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO applications (username, job_title, company, applicant_name, email, phone, cover_letter) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)");
        ps.setString(1, username);
        ps.setString(2, jobTitle);
        ps.setString(3, company);
        ps.setString(4, name);
        ps.setString(5, email);
        ps.setString(6, phone);
        ps.setString(7, coverLetter);
        int result = ps.executeUpdate();
        ps.close();
        
        // Notify admin about new application
        if (result > 0) {
            String adminMessage = "New application from " + name + " (" + username + ") for job: " + jobTitle;
            addNotification("admin", jobTitle, adminMessage, "Pending");
        }
        
        return result > 0;
    }
    
    public boolean updateApplicationStatus(int appId, String status) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "UPDATE applications SET status = ? WHERE id = ?");
        ps.setString(1, status);
        ps.setInt(2, appId);
        int result = ps.executeUpdate();
        ps.close();
        return result > 0;
    }
    
    // ============== NOTIFICATION OPERATIONS ==============
    
    public int getUnreadNotificationCount(String username) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "SELECT COUNT(*) FROM notifications WHERE username = ? AND is_read = 0");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close();
        ps.close();
        return count;
    }
    
    public List<Object[]> getUnreadNotifications(String username) throws SQLException {
        List<Object[]> notifications = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(
            "SELECT id, job_title, message, status, created_at FROM notifications " +
            "WHERE username = ? AND is_read = 0 ORDER BY created_at DESC");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            notifications.add(new Object[]{
                rs.getInt("id"),
                rs.getString("job_title"),
                rs.getString("message"),
                rs.getString("status"),
                rs.getString("created_at")
            });
        }
        rs.close();
        ps.close();
        return notifications;
    }
    
    public List<Object[]> getAllNotifications(String username) throws SQLException {
        List<Object[]> notifications = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(
            "SELECT id, job_title, message, status, is_read, created_at FROM notifications " +
            "WHERE username = ? ORDER BY created_at DESC");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            notifications.add(new Object[]{
                rs.getInt("id"),
                rs.getString("job_title"),
                rs.getString("message"),
                rs.getString("status"),
                rs.getInt("is_read"),
                rs.getString("created_at")
            });
        }
        rs.close();
        ps.close();
        return notifications;
    }
    
    public boolean addNotification(String username, String jobTitle, String message, String status) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO notifications (username, job_title, message, status) VALUES (?, ?, ?, ?)");
        ps.setString(1, username);
        ps.setString(2, jobTitle);
        ps.setString(3, message);
        ps.setString(4, status);
        int result = ps.executeUpdate();
        ps.close();
        return result > 0;
    }
    
    public boolean markNotificationAsRead(int notificationId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "UPDATE notifications SET is_read = 1 WHERE id = ?");
        ps.setInt(1, notificationId);
        int result = ps.executeUpdate();
        ps.close();
        return result > 0;
    }
    
    public boolean markAllNotificationsAsRead(String username) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "UPDATE notifications SET is_read = 1 WHERE username = ?");
        ps.setString(1, username);
        int result = ps.executeUpdate();
        ps.close();
        return result > 0;
    }
    
    // ============== RESUME OPERATIONS ==============
    
    public boolean saveOrUpdateResume(String username, String fullName, String email, String phone,
                                     String address, String education, String experience, 
                                     String skills, String summary) throws SQLException {
        // Check if resume exists
        PreparedStatement checkPs = connection.prepareStatement(
            "SELECT COUNT(*) FROM resumes WHERE username = ?");
        checkPs.setString(1, username);
        ResultSet rs = checkPs.executeQuery();
        boolean exists = rs.next() && rs.getInt(1) > 0;
        rs.close();
        checkPs.close();
        
        if (exists) {
            // Update existing resume
            PreparedStatement ps = connection.prepareStatement(
                "UPDATE resumes SET full_name = ?, email = ?, phone = ?, address = ?, " +
                "education = ?, experience = ?, skills = ?, summary = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE username = ?");
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, address);
            ps.setString(5, education);
            ps.setString(6, experience);
            ps.setString(7, skills);
            ps.setString(8, summary);
            ps.setString(9, username);
            int result = ps.executeUpdate();
            ps.close();
            return result > 0;
        } else {
            // Insert new resume
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO resumes (username, full_name, email, phone, address, education, experience, skills, summary) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, username);
            ps.setString(2, fullName);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setString(5, address);
            ps.setString(6, education);
            ps.setString(7, experience);
            ps.setString(8, skills);
            ps.setString(9, summary);
            int result = ps.executeUpdate();
            ps.close();
            return result > 0;
        }
    }
    
    public Object[] getResume(String username) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "SELECT full_name, email, phone, address, education, experience, skills, summary " +
            "FROM resumes WHERE username = ?");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        Object[] resume = null;
        if (rs.next()) {
            resume = new Object[]{
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("education"),
                rs.getString("experience"),
                rs.getString("skills"),
                rs.getString("summary")
            };
        }
        rs.close();
        ps.close();
        return resume;
    }
    
    public boolean resumeExists(String username) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "SELECT COUNT(*) FROM resumes WHERE username = ?");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next() && rs.getInt(1) > 0;
        rs.close();
        ps.close();
        return exists;
    }
    
    // ============== CONTACT MESSAGE OPERATIONS ==============
    
    public boolean addContactMessage(String username, String subject, String message, String email, String phone) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO contact_messages (username, subject, message, email, phone) VALUES (?, ?, ?, ?, ?)");
        ps.setString(1, username);
        ps.setString(2, subject);
        ps.setString(3, message);
        ps.setString(4, email);
        ps.setString(5, phone);
        int result = ps.executeUpdate();
        ps.close();
        
        // Notify admin about new contact message
        if (result > 0) {
            String adminMessage = "New contact message from " + username + ": " + subject;
            addNotification("admin", "Contact Us", adminMessage, "New");
        }
        
        return result > 0;
    }
    
    public List<Object[]> getAllContactMessages() throws SQLException {
        List<Object[]> messages = new ArrayList<>();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT id, username, subject, message, email, phone, status, is_read, admin_response, created_at " +
            "FROM contact_messages ORDER BY created_at DESC");
        while (rs.next()) {
            messages.add(new Object[]{
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("subject"),
                rs.getString("message"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("status"),
                rs.getInt("is_read"),
                rs.getString("admin_response"),
                rs.getString("created_at")
            });
        }
        rs.close();
        stmt.close();
        return messages;
    }
    
    public List<Object[]> getUserContactMessages(String username) throws SQLException {
        List<Object[]> messages = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(
            "SELECT id, subject, message, status, admin_response, created_at " +
            "FROM contact_messages WHERE username = ? ORDER BY created_at DESC");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            messages.add(new Object[]{
                rs.getInt("id"),
                rs.getString("subject"),
                rs.getString("message"),
                rs.getString("status"),
                rs.getString("admin_response"),
                rs.getString("created_at")
            });
        }
        rs.close();
        ps.close();
        return messages;
    }
    
    public int getUnreadContactMessageCount() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM contact_messages WHERE is_read = 0");
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close();
        stmt.close();
        return count;
    }
    
    public boolean markContactMessageAsRead(int messageId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "UPDATE contact_messages SET is_read = 1 WHERE id = ?");
        ps.setInt(1, messageId);
        int result = ps.executeUpdate();
        ps.close();
        return result > 0;
    }
    
    public boolean updateContactMessageStatus(int messageId, String status, String adminResponse) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
            "UPDATE contact_messages SET status = ?, admin_response = ?, is_read = 1 WHERE id = ?");
        ps.setString(1, status);
        ps.setString(2, adminResponse);
        ps.setInt(3, messageId);
        int result = ps.executeUpdate();
        ps.close();
        
        // Notify user about response
        if (result > 0 && adminResponse != null && !adminResponse.trim().isEmpty()) {
            PreparedStatement getPs = connection.prepareStatement("SELECT username, subject FROM contact_messages WHERE id = ?");
            getPs.setInt(1, messageId);
            ResultSet rs = getPs.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                String subject = rs.getString("subject");
                String userMessage = "Admin responded to your contact message: " + subject;
                addNotification(username, "Contact Us", userMessage, status);
            }
            rs.close();
            getPs.close();
        }
        
        return result > 0;
    }
    
    // ============== USER MANAGEMENT OPERATIONS ==============
    
    public List<Object[]> getAllUsersWithSessions() throws SQLException {
        List<Object[]> users = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Statement stmt = connection.createStatement();
        // Get all users with their latest session info including passwords
        ResultSet rs = stmt.executeQuery(
            "SELECT u.id, u.username, u.password, u.role, " +
            "COALESCE(MAX(CASE WHEN s.is_active = 1 THEN 'Online' END), 'Offline') as status, " +
            "MAX(s.login_time) as last_login, " +
            "MAX(s.logout_time) as last_logout " +
            "FROM users u " +
            "LEFT JOIN user_sessions s ON u.username = s.username " +
            "GROUP BY u.id, u.username, u.password, u.role " +
            "ORDER BY u.username");
        
        while (rs.next()) {
            String status = rs.getString("status");
            if (status == null) status = "Offline";
            
            String lastLogin = "Never";
            Timestamp loginTs = rs.getTimestamp("last_login");
            if (loginTs != null) {
                lastLogin = dateFormat.format(loginTs);
            }
            
            String lastLogout = "N/A";
            Timestamp logoutTs = rs.getTimestamp("last_logout");
            if (logoutTs != null) {
                lastLogout = dateFormat.format(logoutTs);
            }
            
            users.add(new Object[]{
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"), // Include password
                rs.getString("role"),
                status,
                lastLogin,
                lastLogout
            });
        }
        rs.close();
        stmt.close();
        return users;
    }
    
    public List<Object[]> getActiveUsers() throws SQLException {
        List<Object[]> activeUsers = new ArrayList<>();
        PreparedStatement ps = connection.prepareStatement(
            "SELECT DISTINCT s.username, u.role, s.login_time " +
            "FROM user_sessions s " +
            "JOIN users u ON s.username = u.username " +
            "WHERE s.is_active = 1 " +
            "ORDER BY s.login_time DESC");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            activeUsers.add(new Object[]{
                rs.getString("username"),
                rs.getString("role"),
                rs.getString("login_time")
            });
        }
        rs.close();
        ps.close();
        return activeUsers;
    }
    
    public int getActiveUserCount() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT COUNT(DISTINCT username) FROM user_sessions WHERE is_active = 1");
        int count = rs.next() ? rs.getInt(1) : 0;
        rs.close();
        stmt.close();
        return count;
    }
}
