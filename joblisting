import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * JobListingApp.java - Main Application
 * 
 * Modern Job Listing System with H2 Database.
 * 
 * Files:
 * - AppTheme.java       : Colors and Fonts
 * - UIHelper.java       : UI Component Factory  
 * - DialogManager.java  : Application Dialogs
 * - DatabaseManager.java: Database Operations
 * - JobListingApp.java  : Main Application (this file)
 * 
 * Compile: javac -cp ".;h2-2.2.224.jar" *.java
 * Run: java -cp ".;h2-2.2.224.jar" JobListingApp
 */
public class JobListingApp extends JFrame {
    
    // Components
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField tfTitle, tfCompany, tfLocation, tfSalary, tfSearch;
    private JTextArea taDescription;
    private JButton btnDelete;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Managers
    private DatabaseManager dbManager;
    private DialogManager dialogManager;
    
    // User Session
    private String currentUser = null;
    private String currentUserRole = null;

    public JobListingApp() {
        super("Job Listing System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppTheme.BG_COLOR);
        
        initializeDatabase();
        showAuthScreen();
        setVisible(true);
    }
    
    private void initializeDatabase() {
        try {
            dbManager = DatabaseManager.getInstance();
            if (dbManager.isConnected()) {
                System.out.println("✓ Database initialized successfully!");
                dbManager.addSampleJobs();
            } else {
                UIHelper.showErrorDialog(this, "Failed to connect to database.");
            }
        } catch (Exception e) {
            UIHelper.showErrorDialog(this, "Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============== AUTH SCREEN ==============
    
    private void showAuthScreen() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        setSize(450, 500);
        getContentPane().setBackground(AppTheme.BG_COLOR);
        
        JPanel mainPanel = new JPanel(new CardLayout());
        mainPanel.setOpaque(false);
        
        mainPanel.add(createLoginPanel(mainPanel), "LOGIN");
        mainPanel.add(createSignupPanel(mainPanel), "SIGNUP");
        
        add(mainPanel, BorderLayout.CENTER);
        ((CardLayout) mainPanel.getLayout()).show(mainPanel, "LOGIN");
        
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    private JPanel createLoginPanel(JPanel mainPanel) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        
        JPanel card = UIHelper.createCardPanel(40);
        ((EmptyBorder) ((CompoundBorder) card.getBorder()).getInsideBorder()).getBorderInsets(card);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(AppTheme.BORDER_COLOR, 1),
            new EmptyBorder(40, 50, 40, 50)
        ));
        
        // Logo
        JLabel iconLabel = new JLabel("💼");
        iconLabel.setFont(AppTheme.FONT_EMOJI);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));
        
        // Title
        JLabel titleLabel = new JLabel("Job Listing");
        titleLabel.setFont(AppTheme.FONT_TITLE);
        titleLabel.setForeground(AppTheme.PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        
        // DB Status
        JLabel dbStatus = new JLabel(dbManager != null && dbManager.isConnected() ? "🟢 Database Connected" : "🔴 Database Disconnected");
        dbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dbStatus.setForeground(AppTheme.TEXT_SECONDARY);
        dbStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(dbStatus);
        
        JLabel subtitleLabel = new JLabel("Sign in to your account");
        subtitleLabel.setFont(AppTheme.FONT_SUBTITLE);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(30));
        
        // Username
        JLabel usernameLabel = UIHelper.createStyledLabel("Username");
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(usernameLabel);
        card.add(Box.createVerticalStrut(5));
        
        JTextField tfUsername = UIHelper.createStyledTextField(20);
        tfUsername.setMaximumSize(new Dimension(280, 40));
        tfUsername.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(tfUsername);
        card.add(Box.createVerticalStrut(15));
        
        // Password
        JLabel passwordLabel = UIHelper.createStyledLabel("Password");
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(passwordLabel);
        card.add(Box.createVerticalStrut(5));
        
        JPasswordField pfPassword = UIHelper.createStyledPasswordField(20);
        pfPassword.setMaximumSize(new Dimension(280, 40));
        pfPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(pfPassword);
        card.add(Box.createVerticalStrut(8));
        
        // Show Password
        JCheckBox cbShowPassword = new JCheckBox("Show Password");
        cbShowPassword.setFont(AppTheme.FONT_SUBTITLE);
        cbShowPassword.setForeground(AppTheme.TEXT_SECONDARY);
        cbShowPassword.setBackground(AppTheme.CARD_COLOR);
        cbShowPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        cbShowPassword.addActionListener(e -> pfPassword.setEchoChar(cbShowPassword.isSelected() ? (char) 0 : '●'));
        card.add(cbShowPassword);
        card.add(Box.createVerticalStrut(15));
        
        // Login button
        JButton btnLogin = UIHelper.createStyledButton("Sign In", AppTheme.PRIMARY_COLOR);
        btnLogin.setMaximumSize(new Dimension(280, 42));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> {
            String username = tfUsername.getText().trim();
            String password = new String(pfPassword.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                UIHelper.showErrorDialog(this, "Please fill in all fields.");
                return;
            }
            
            if (login(username, password)) {
                currentUser = username;
                dialogManager = new DialogManager(this, dbManager, currentUser);
                dialogManager.setRefreshCallback(() -> showJobListingScreen());
                showJobListingScreen();
            }
        });
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(15));
        
        // Signup link
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        linkPanel.setOpaque(false);
        linkPanel.add(new JLabel("Don't have an account?") {{ setFont(AppTheme.FONT_SUBTITLE); setForeground(AppTheme.TEXT_SECONDARY); }});
        
        JLabel signupLink = new JLabel("Sign Up");
        signupLink.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signupLink.setForeground(AppTheme.PRIMARY_COLOR);
        signupLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signupLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { ((CardLayout) mainPanel.getLayout()).show(mainPanel, "SIGNUP"); }
            public void mouseEntered(MouseEvent e) { signupLink.setForeground(AppTheme.PRIMARY_DARK); }
            public void mouseExited(MouseEvent e) { signupLink.setForeground(AppTheme.PRIMARY_COLOR); }
        });
        linkPanel.add(signupLink);
        linkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(linkPanel);
        
        wrapper.add(card);
        return wrapper;
    }

    private JPanel createSignupPanel(JPanel mainPanel) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        
        JPanel card = UIHelper.createCardPanel(30);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(AppTheme.BORDER_COLOR, 1),
            new EmptyBorder(30, 50, 30, 50)
        ));
        
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(AppTheme.FONT_TITLE);
        titleLabel.setForeground(AppTheme.PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        
        JLabel subtitleLabel = new JLabel("Join our job listing platform");
        subtitleLabel.setFont(AppTheme.FONT_SUBTITLE);
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(25));
        
        // Username
        card.add(createCenteredLabel("Username"));
        card.add(Box.createVerticalStrut(5));
        JTextField tfUsername = UIHelper.createStyledTextField(20);
        tfUsername.setMaximumSize(new Dimension(280, 40));
        tfUsername.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(tfUsername);
        card.add(Box.createVerticalStrut(12));
        
        // Password
        card.add(createCenteredLabel("Password"));
        card.add(Box.createVerticalStrut(5));
        JPasswordField pfPassword = UIHelper.createStyledPasswordField(20);
        pfPassword.setMaximumSize(new Dimension(280, 40));
        pfPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(pfPassword);
        card.add(Box.createVerticalStrut(12));
        
        // Confirm Password
        card.add(createCenteredLabel("Confirm Password"));
        card.add(Box.createVerticalStrut(5));
        JPasswordField pfConfirmPassword = UIHelper.createStyledPasswordField(20);
        pfConfirmPassword.setMaximumSize(new Dimension(280, 40));
        pfConfirmPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(pfConfirmPassword);
        card.add(Box.createVerticalStrut(12));
        
        // Signup button
        JButton btnSignup = UIHelper.createStyledButton("Create Account", AppTheme.ACCENT_COLOR);
        btnSignup.setMaximumSize(new Dimension(280, 42));
        btnSignup.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSignup.addActionListener(e -> {
            String username = tfUsername.getText().trim();
            String password = new String(pfPassword.getPassword());
            String confirmPassword = new String(pfConfirmPassword.getPassword());
            
            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                UIHelper.showErrorDialog(this, "Please fill in all fields.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                UIHelper.showErrorDialog(this, "Passwords do not match.");
                return;
            }
            if (signup(username, password, "Customer")) {
                UIHelper.showSuccessDialog(this, "Account created! Please sign in.");
                ((CardLayout) mainPanel.getLayout()).show(mainPanel, "LOGIN");
                tfUsername.setText(""); pfPassword.setText(""); pfConfirmPassword.setText("");
            }
        });
        card.add(btnSignup);
        card.add(Box.createVerticalStrut(15));
        
        // Login link
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        linkPanel.setOpaque(false);
        linkPanel.add(new JLabel("Already have an account?") {{ setFont(AppTheme.FONT_SUBTITLE); setForeground(AppTheme.TEXT_SECONDARY); }});
        
        JLabel loginLink = new JLabel("Sign In");
        loginLink.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginLink.setForeground(AppTheme.PRIMARY_COLOR);
        loginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { ((CardLayout) mainPanel.getLayout()).show(mainPanel, "LOGIN"); }
            public void mouseEntered(MouseEvent e) { loginLink.setForeground(AppTheme.PRIMARY_DARK); }
            public void mouseExited(MouseEvent e) { loginLink.setForeground(AppTheme.PRIMARY_COLOR); }
        });
        linkPanel.add(loginLink);
        linkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(linkPanel);
        
        wrapper.add(card);
        return wrapper;
    }
    
    private JLabel createCenteredLabel(String text) {
        JLabel label = UIHelper.createStyledLabel(text);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private boolean signup(String username, String password, String role) {
        try {
            if (dbManager.userExists(username)) {
                UIHelper.showErrorDialog(this, "Username already exists!");
                return false;
            }
            return dbManager.addUser(username, password, role);
        } catch (SQLException e) {
            UIHelper.showErrorDialog(this, "Database error: " + e.getMessage());
            return false;
        }
    }

    private boolean login(String username, String password) {
        // Test users (work without database)
        if ("admin".equals(username) && "admin".equals(password)) {
            currentUserRole = "Admin";
            return true;
        }
        if ("user".equals(username) && "user".equals(password)) {
            currentUserRole = "Customer";
            return true;
        }
        
        // Database login
        try {
            if (dbManager != null && dbManager.isConnected()) {
                String role = dbManager.validateLogin(username, password);
                if (role != null) {
                    currentUserRole = role;
                    return true;
                }
            }
            UIHelper.showErrorDialog(this, "Invalid username or password!");
            return false;
        } catch (SQLException e) {
            UIHelper.showErrorDialog(this, "Database error: " + e.getMessage());
            return false;
        }
    }

    // ============== MAIN JOB LISTING SCREEN ==============
    
    private void showJobListingScreen() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppTheme.BG_COLOR);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        
        boolean isAdmin = "Admin".equals(currentUserRole);
        
        // Header
        add(createHeaderPanel(isAdmin), BorderLayout.NORTH);
        
        // Content
        add(createContentPanel(), BorderLayout.CENTER);
        
        // Side Panel
        if (isAdmin) {
            add(createAdminFormPanel(), BorderLayout.EAST);
            attachListeners();
        } else {
            add(createCustomerPanel(), BorderLayout.EAST);
        }
        
        loadJobsFromDatabase();
        
        revalidate();
        repaint();
        
        if (!isAdmin) {
            SwingUtilities.invokeLater(this::checkCustomerNotifications);
        }
    }
    
    private JPanel createHeaderPanel(boolean isAdmin) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppTheme.PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 70));
        headerPanel.setBorder(new EmptyBorder(0, 25, 0, 25));
        
        // Left
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15));
        leftHeader.setOpaque(false);
        JLabel appTitle = new JLabel("💼 Job Listing System");
        appTitle.setFont(AppTheme.FONT_HEADER);
        appTitle.setForeground(Color.WHITE);
        leftHeader.add(appTitle);
        
        JLabel dbIndicator = new JLabel("  🗄️ H2 Database");
        dbIndicator.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dbIndicator.setForeground(new Color(255, 255, 255, 180));
        leftHeader.add(dbIndicator);
        headerPanel.add(leftHeader, BorderLayout.WEST);
        
        // Right
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        rightHeader.setOpaque(false);
        
        JLabel userLabel = new JLabel("👤 " + currentUser);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        rightHeader.add(userLabel);
        
        JLabel roleLabel = new JLabel(isAdmin ? "🔐 Admin" : "👥 Customer");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roleLabel.setForeground(new Color(255, 255, 255, 200));
        roleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        rightHeader.add(roleLabel);
        
        // Notifications
        if (!isAdmin) {
            try {
                int unreadCount = dbManager.getUnreadNotificationCount(currentUser);
                String notifText = unreadCount > 0 ? "🔔 (" + unreadCount + ")" : "🔔";
                Color notifColor = unreadCount > 0 ? AppTheme.DANGER_COLOR : new Color(149, 165, 166);
                JButton btnNotif = UIHelper.createStyledButton(notifText, notifColor);
                btnNotif.setPreferredSize(new Dimension(80, 32));
                btnNotif.addActionListener(e -> dialogManager.showNotificationsDialog());
                rightHeader.add(btnNotif);
            } catch (SQLException e) { }
        }
        
        JButton btnLogout = UIHelper.createStyledButton("Logout", AppTheme.DANGER_COLOR);
        btnLogout.setPreferredSize(new Dimension(90, 32));
        btnLogout.addActionListener(e -> { currentUser = null; currentUserRole = null; showAuthScreen(); });
        rightHeader.add(btnLogout);
        
        headerPanel.add(rightHeader, BorderLayout.EAST);
        return headerPanel;
    }
    
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        // Search
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        JLabel searchIcon = new JLabel("🔍 Search Jobs:");
        searchIcon.setFont(AppTheme.FONT_LABEL);
        searchIcon.setForeground(AppTheme.TEXT_PRIMARY);
        searchPanel.add(searchIcon, BorderLayout.WEST);
        
        tfSearch = UIHelper.createStyledTextField(30);
        tfSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterTable(tfSearch.getText()); }
        });
        searchPanel.add(tfSearch, BorderLayout.CENTER);
        
        JLabel jobCount = new JLabel("Total Jobs: 0");
        jobCount.setFont(AppTheme.FONT_SUBTITLE);
        jobCount.setForeground(AppTheme.TEXT_SECONDARY);
        searchPanel.add(jobCount, BorderLayout.EAST);
        
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Table
        tableModel = new DefaultTableModel(AppTheme.JOB_COLUMNS, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(tableModel);
        table.setFont(AppTheme.FONT_TABLE);
        table.setRowHeight(40);
        table.setShowGrid(true);
        table.setGridColor(AppTheme.BORDER_COLOR);
        table.setSelectionBackground(new Color(52, 152, 219, 80));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        
        // Header renderer
        JTableHeader header = table.getTableHeader();
        header.setFont(AppTheme.FONT_TABLE_HEADER);
        header.setBackground(AppTheme.PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setBackground(AppTheme.PRIMARY_COLOR);
                l.setForeground(Color.WHITE);
                l.setFont(AppTheme.FONT_TABLE_HEADER);
                l.setHorizontalAlignment(JLabel.CENTER);
                l.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 1, AppTheme.PRIMARY_DARK));
                l.setOpaque(true);
                return l;
            }
        });
        
        // Cell renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setBackground(s ? new Color(52, 152, 219, 100) : (r % 2 == 0 ? Color.WHITE : new Color(240, 248, 255)));
                l.setForeground(Color.BLACK);
                l.setFont(AppTheme.FONT_TABLE);
                l.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                l.setHorizontalAlignment(c == 0 ? JLabel.CENTER : JLabel.LEFT);
                l.setOpaque(true);
                return l;
            }
        });
        
        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(200);
        
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        
        // Professional features: Double-click and context menu
        setupContextMenu();
        
        // Keyboard shortcuts
        setupKeyboardShortcuts();
        
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR, 1));
        contentPanel.add(tableScroll, BorderLayout.CENTER);
        
        tableModel.addTableModelListener(e -> jobCount.setText("Total Jobs: " + tableModel.getRowCount()));
        
        return contentPanel;
    }
    
    private JPanel createAdminFormPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setPreferredSize(new Dimension(320, 0));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(20, 0, 20, 25));
        
        JPanel formCard = UIHelper.createCardPanel(20);
        
        // Statistics Panel
        JPanel statsPanel = createStatisticsPanel();
        formCard.add(statsPanel);
        formCard.add(Box.createVerticalStrut(20));
        
        // Action buttons
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setOpaque(false);
        
        btnDelete = UIHelper.createStyledButton("🗑️ Delete Job", AppTheme.DANGER_COLOR);
        btnDelete.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnDelete.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPanel.add(btnDelete);
        btnPanel.add(Box.createVerticalStrut(10));
        
        JButton btnExport = UIHelper.createStyledButton("📥 Export to CSV", new Color(46, 125, 50));
        btnExport.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnExport.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExport.addActionListener(e -> exportToCSV());
        btnPanel.add(btnExport);
        btnPanel.add(Box.createVerticalStrut(10));
        
        JButton btnRefresh = UIHelper.createStyledButton("🔄 Refresh", AppTheme.PRIMARY_COLOR);
        btnRefresh.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnRefresh.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRefresh.addActionListener(e -> loadJobsFromDatabase());
        btnPanel.add(btnRefresh);
        
        formCard.add(btnPanel);
        
        wrapper.add(formCard, BorderLayout.NORTH);
        return wrapper;
    }
    
    private JPanel createStatisticsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER_COLOR, 1),
            "📊 Statistics",
            0, 0,
            AppTheme.FONT_LABEL,
            AppTheme.TEXT_PRIMARY
        ));
        
        try {
            int totalJobs = dbManager.getAllJobs().size();
            int totalApplications = dbManager.getAllApplications().size();
            
            JLabel totalJobsLabel = new JLabel("Total Jobs: " + totalJobs);
            totalJobsLabel.setFont(AppTheme.FONT_SUBTITLE);
            totalJobsLabel.setForeground(AppTheme.TEXT_PRIMARY);
            statsPanel.add(totalJobsLabel);
            statsPanel.add(Box.createVerticalStrut(8));
            
            JLabel totalAppsLabel = new JLabel("Total Applications: " + totalApplications);
            totalAppsLabel.setFont(AppTheme.FONT_SUBTITLE);
            totalAppsLabel.setForeground(AppTheme.TEXT_PRIMARY);
            statsPanel.add(totalAppsLabel);
            statsPanel.add(Box.createVerticalStrut(8));
            
            JLabel tableCountLabel = new JLabel("Visible: " + tableModel.getRowCount());
            tableCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            tableCountLabel.setForeground(AppTheme.TEXT_SECONDARY);
            statsPanel.add(tableCountLabel);
        } catch (SQLException e) {
            JLabel errorLabel = new JLabel("Error loading stats");
            errorLabel.setForeground(AppTheme.DANGER_COLOR);
            statsPanel.add(errorLabel);
        }
        
        return statsPanel;
    }
    
    private JPanel createCustomerPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setPreferredSize(new Dimension(280, 0));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(20, 0, 20, 25));
        
        JPanel infoCard = UIHelper.createCardPanel(25);
        
        JLabel infoIcon = new JLabel("💼");
        infoIcon.setFont(AppTheme.FONT_EMOJI_SMALL);
        infoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoCard.add(infoIcon);
        infoCard.add(Box.createVerticalStrut(10));
        
        JLabel infoTitle = new JLabel("Customer Mode");
        infoTitle.setFont(AppTheme.FONT_DIALOG_HEADER);
        infoTitle.setForeground(AppTheme.TEXT_PRIMARY);
        infoTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoCard.add(infoTitle);
        infoCard.add(Box.createVerticalStrut(10));
        
        JLabel infoText = new JLabel("<html><center>✅ Browse jobs<br>✅ Search jobs<br>✅ Apply for jobs<br>✅ Manage resume</center></html>");
        infoText.setFont(AppTheme.FONT_SUBTITLE);
        infoText.setForeground(AppTheme.TEXT_SECONDARY);
        infoText.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoCard.add(infoText);
        infoCard.add(Box.createVerticalStrut(20));
        
        JButton btnApply = UIHelper.createStyledButton("📝 Apply for Job", AppTheme.ACCENT_COLOR);
        btnApply.setMaximumSize(new Dimension(200, 40));
        btnApply.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnApply.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                UIHelper.showWarningDialog(this, "Please select a job first.");
                return;
            }
            int modelRow = table.convertRowIndexToModel(row);
            String jobTitle = String.valueOf(tableModel.getValueAt(modelRow, 1));
            String company = String.valueOf(tableModel.getValueAt(modelRow, 2));
            dialogManager.showApplyJobDialog(jobTitle, company);
        });
        infoCard.add(btnApply);
        infoCard.add(Box.createVerticalStrut(10));
        
        JButton btnMyApps = UIHelper.createStyledButton("📋 My Applications", AppTheme.PRIMARY_COLOR);
        btnMyApps.setMaximumSize(new Dimension(200, 40));
        btnMyApps.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnMyApps.addActionListener(e -> dialogManager.showMyApplicationsDialog());
        infoCard.add(btnMyApps);
        infoCard.add(Box.createVerticalStrut(10));
        
        JButton btnResume = UIHelper.createStyledButton("📄 My Resume", new Color(46, 125, 50));
        btnResume.setMaximumSize(new Dimension(200, 40));
        btnResume.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnResume.addActionListener(e -> dialogManager.showResumeDialog());
        infoCard.add(btnResume);
        
        wrapper.add(infoCard, BorderLayout.NORTH);
        return wrapper;
    }
    
    private void loadJobsFromDatabase() {
        try {
            tableModel.setRowCount(0);
            List<String[]> jobs = dbManager.getAllJobs();
            for (String[] job : jobs) {
                tableModel.addRow(job);
            }
            System.out.println("✓ Loaded " + jobs.size() + " jobs");
        } catch (SQLException e) {
            UIHelper.showErrorDialog(this, "Error loading jobs: " + e.getMessage());
        }
    }
    
    private void filterTable(String query) {
        sorter.setRowFilter(query.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + query));
    }

    private void attachListeners() {
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { UIHelper.showWarningDialog(this, "Please select a job."); return; }
            
            if (UIHelper.showConfirmDialog(this, "Delete this job?", "Confirm") == JOptionPane.YES_OPTION) {
                int modelRow = table.convertRowIndexToModel(row);
                int jobId = Integer.parseInt(String.valueOf(tableModel.getValueAt(modelRow, 0)));
                
                try {
                    if (dbManager.deleteJob(jobId)) {
                        tableModel.removeRow(modelRow);
                        clearForm();
                        UIHelper.showSuccessDialog(this, "Job deleted!");
                    }
                } catch (SQLException ex) { UIHelper.showErrorDialog(this, "Error: " + ex.getMessage()); }
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) return;
            int modelRow = table.convertRowIndexToModel(row);
            tfTitle.setText(String.valueOf(tableModel.getValueAt(modelRow, 1)));
            tfCompany.setText(String.valueOf(tableModel.getValueAt(modelRow, 2)));
            tfLocation.setText(String.valueOf(tableModel.getValueAt(modelRow, 3)));
            tfSalary.setText(String.valueOf(tableModel.getValueAt(modelRow, 4)));
            taDescription.setText(String.valueOf(tableModel.getValueAt(modelRow, 5)));
        });
    }

    private void clearForm() {
        tfTitle.setText(""); tfCompany.setText(""); tfLocation.setText("");
        tfSalary.setText(""); taDescription.setText(""); table.clearSelection();
    }
    
    // ============== PROFESSIONAL FEATURES ==============
    
    private void setupKeyboardShortcuts() {
        // Ctrl+F to focus search
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "focusSearch");
        getRootPane().getActionMap().put("focusSearch", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                tfSearch.requestFocus();
                tfSearch.selectAll();
            }
        });
        
        // Delete key to delete selected job (Admin only)
        if ("Admin".equals(currentUserRole)) {
            table.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteJob");
            table.getActionMap().put("deleteJob", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (btnDelete != null) btnDelete.doClick();
                }
            });
        }
        
        // Esc to clear search
        tfSearch.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSearch");
        tfSearch.getActionMap().put("clearSearch", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                tfSearch.setText("");
                filterTable("");
            }
        });
    }
    
    private void setupContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();
        
        JMenuItem viewDetails = new JMenuItem("👁️ View Details");
        viewDetails.addActionListener(e -> showJobDetailsDialog());
        contextMenu.add(viewDetails);
        
        if ("Admin".equals(currentUserRole)) {
            contextMenu.addSeparator();
            JMenuItem deleteItem = new JMenuItem("🗑️ Delete");
            deleteItem.addActionListener(e -> {
                if (btnDelete != null) btnDelete.doClick();
            });
            contextMenu.add(deleteItem);
        }
        
        if (!"Admin".equals(currentUserRole)) {
            contextMenu.addSeparator();
            JMenuItem applyItem = new JMenuItem("📝 Apply for Job");
            applyItem.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row >= 0 && dialogManager != null) {
                    int modelRow = table.convertRowIndexToModel(row);
                    String jobTitle = String.valueOf(tableModel.getValueAt(modelRow, 1));
                    String company = String.valueOf(tableModel.getValueAt(modelRow, 2));
                    dialogManager.showApplyJobDialog(jobTitle, company);
                }
            });
            contextMenu.add(applyItem);
        }
        
        contextMenu.addSeparator();
        JMenuItem exportItem = new JMenuItem("📥 Export to CSV");
        exportItem.addActionListener(e -> exportToCSV());
        contextMenu.add(exportItem);
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Double-click to view details
                if (e.getClickCount() == 2) {
                    showJobDetailsDialog();
                }
            }
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) showContextMenu(e);
            }
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showContextMenu(e);
            }
            private void showContextMenu(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                    contextMenu.show(table, e.getX(), e.getY());
                }
            }
        });
    }
    
    private void showJobDetailsDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            UIHelper.showWarningDialog(this, "Please select a job to view details.");
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(row);
        String id = String.valueOf(tableModel.getValueAt(modelRow, 0));
        String title = String.valueOf(tableModel.getValueAt(modelRow, 1));
        String company = String.valueOf(tableModel.getValueAt(modelRow, 2));
        String location = String.valueOf(tableModel.getValueAt(modelRow, 3));
        String salary = String.valueOf(tableModel.getValueAt(modelRow, 4));
        String description = String.valueOf(tableModel.getValueAt(modelRow, 5));
        
        JDialog dialog = new JDialog(this, "Job Details", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(AppTheme.BG_COLOR);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppTheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("📋 Job Details");
        headerLabel.setFont(AppTheme.FONT_HEADER);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = UIHelper.createCardPanel(25);
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        addDetailRow(contentPanel, "ID:", id);
        addDetailRow(contentPanel, "Title:", title);
        addDetailRow(contentPanel, "Company:", company);
        addDetailRow(contentPanel, "Location:", location);
        addDetailRow(contentPanel, "Salary:", salary);
        
        contentPanel.add(Box.createVerticalStrut(10));
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(AppTheme.FONT_LABEL);
        descLabel.setForeground(AppTheme.TEXT_PRIMARY);
        contentPanel.add(descLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        
        JTextArea descArea = new JTextArea(description);
        descArea.setFont(AppTheme.FONT_SUBTITLE);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR, 1));
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        contentPanel.add(descScroll);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        
        // Close button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        JButton btnClose = UIHelper.createStyledButton("Close", AppTheme.PRIMARY_COLOR);
        btnClose.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void addDetailRow(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_LABEL);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        lbl.setPreferredSize(new Dimension(80, 20));
        row.add(lbl, BorderLayout.WEST);
        
        JLabel val = new JLabel(value);
        val.setFont(AppTheme.FONT_SUBTITLE);
        val.setForeground(AppTheme.TEXT_SECONDARY);
        row.add(val, BorderLayout.CENTER);
        
        panel.add(row);
        panel.add(Box.createVerticalStrut(8));
    }
    
    private void exportToCSV() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Jobs to CSV");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            fileChooser.setSelectedFile(new java.io.File("jobs_export_" + sdf.format(new Date()) + ".csv"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new java.io.File(file.getAbsolutePath() + ".csv");
                }
                
                try (FileWriter writer = new FileWriter(file)) {
                    // Write header
                    for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        writer.append(tableModel.getColumnName(i));
                        if (i < tableModel.getColumnCount() - 1) writer.append(",");
                    }
                    writer.append("\n");
                    
                    // Write data
                    for (int row = 0; row < tableModel.getRowCount(); row++) {
                        for (int col = 0; col < tableModel.getColumnCount(); col++) {
                            Object value = tableModel.getValueAt(row, col);
                            String strValue = value != null ? value.toString().replace(",", ";") : "";
                            writer.append("\"").append(strValue).append("\"");
                            if (col < tableModel.getColumnCount() - 1) writer.append(",");
                        }
                        writer.append("\n");
                    }
                    
                    UIHelper.showSuccessDialog(this, "Jobs exported successfully to:\n" + file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            UIHelper.showErrorDialog(this, "Error exporting to CSV: " + e.getMessage());
        }
    }
    
    private void checkCustomerNotifications() {
        try {
            List<Object[]> notifications = dbManager.getUnreadNotifications(currentUser);
            if (!notifications.isEmpty()) {
                StringBuilder msg = new StringBuilder("You have " + notifications.size() + " new update(s)!\n\n");
                for (Object[] n : notifications) {
                    msg.append("Approved".equals(n[3]) ? "✅ " : "❌ ").append(n[1]).append("\n");
                }
                msg.append("\nClick 🔔 to see details.");
                JOptionPane.showMessageDialog(this, msg.toString(), "📬 New Notifications!", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) { }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { }
        SwingUtilities.invokeLater(JobListingApp::new);
    }
}
