import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * DialogManager.java - Application Dialogs
 * 
 * Manages all dialog windows for the Job Listing System.
 */
public class DialogManager {
    
    private JFrame parent;
    private DatabaseManager dbManager;
    private String currentUser;
    private Runnable refreshCallback;
    private JobListingApp mainApp; // Reference to main app for opening job list
    
    public DialogManager(JFrame parent, DatabaseManager dbManager, String currentUser) {
        this.parent = parent;
        this.dbManager = dbManager;
        this.currentUser = currentUser;
        if (parent instanceof JobListingApp) {
            this.mainApp = (JobListingApp) parent;
        }
    }
    
    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }
    
    // ============== ADMIN: APPLICATION MANAGEMENT ==============
    
    public void showAdminApplicationsDialog() {
        JDialog dialog = new JDialog(parent, "📋 Job Applications", true);
        dialog.setSize(900, 500);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(AppTheme.BG_COLOR);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppTheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("📋 Application Management");
        headerLabel.setFont(AppTheme.FONT_HEADER);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Applicant", "Job Title", "Company", "Name", "Email", "Phone", "Status", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable table = new JTable(model);
        table.setFont(AppTheme.FONT_TABLE);
        table.setRowHeight(35);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        loadApplicationsToTable(model);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(AppTheme.BG_COLOR);
        
        JButton btnContact = UIHelper.createStyledButton("📞 Contact", new Color(52, 152, 219));
        btnContact.setPreferredSize(new Dimension(120, 35));
        btnContact.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                UIHelper.showWarningDialog(dialog, "Please select an application.");
                return;
            }
            String name = String.valueOf(model.getValueAt(row, 4));
            String email = String.valueOf(model.getValueAt(row, 5));
            String phone = String.valueOf(model.getValueAt(row, 6));
            showContactDialog(dialog, name, email, phone);
        });
        
        JButton btnApprove = UIHelper.createStyledButton("✅ Approve", AppTheme.ACCENT_COLOR);
        btnApprove.setPreferredSize(new Dimension(120, 35));
        btnApprove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                UIHelper.showWarningDialog(dialog, "Please select an application.");
                return;
            }
            int appId = Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
            String username = String.valueOf(model.getValueAt(row, 1));
            String jobTitle = String.valueOf(model.getValueAt(row, 2));
            updateApplicationStatus(appId, username, jobTitle, "Approved", model, row, dialog);
        });
        
        JButton btnReject = UIHelper.createStyledButton("❌ Reject", AppTheme.DANGER_COLOR);
        btnReject.setPreferredSize(new Dimension(120, 35));
        btnReject.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                UIHelper.showWarningDialog(dialog, "Please select an application.");
                return;
            }
            int appId = Integer.parseInt(String.valueOf(model.getValueAt(row, 0)));
            String username = String.valueOf(model.getValueAt(row, 1));
            String jobTitle = String.valueOf(model.getValueAt(row, 2));
            updateApplicationStatus(appId, username, jobTitle, "Rejected", model, row, dialog);
        });
        
        JButton btnRefresh = UIHelper.createStyledButton("🔄 Refresh", new Color(52, 73, 94));
        btnRefresh.setPreferredSize(new Dimension(120, 35));
        btnRefresh.addActionListener(e -> {
            model.setRowCount(0);
            loadApplicationsToTable(model);
        });
        
        JButton btnClose = UIHelper.createStyledButton("Close", new Color(149, 165, 166));
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.addActionListener(e -> dialog.dispose());
        
        btnPanel.add(btnContact);
        btnPanel.add(btnApprove);
        btnPanel.add(btnReject);
        btnPanel.add(btnRefresh);
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void loadApplicationsToTable(DefaultTableModel model) {
        try {
            List<Object[]> apps = dbManager.getAllApplications();
            for (Object[] app : apps) {
                model.addRow(new Object[]{
                    app[0], app[1], app[2], app[3], app[4], app[5], app[6], app[8], app[9]
                });
            }
        } catch (SQLException e) {
            UIHelper.showErrorDialog(parent, "Error loading applications: " + e.getMessage());
        }
    }
    
    private void updateApplicationStatus(int appId, String username, String jobTitle, 
                                         String status, DefaultTableModel model, int row, JDialog dialog) {
        try {
            if (dbManager.updateApplicationStatus(appId, status)) {
                model.setValueAt(status, row, 7);
                String message = "Your application for '" + jobTitle + "' has been " + status.toLowerCase() + ".";
                dbManager.addNotification(username, jobTitle, message, status);
                UIHelper.showSuccessDialog(dialog, "Application " + status.toLowerCase() + "! Customer has been notified.");
            }
        } catch (SQLException e) {
            UIHelper.showErrorDialog(dialog, "Error: " + e.getMessage());
        }
    }
    
    // ============== ADMIN: NOTIFICATIONS ==============
    
    public void showAdminNotificationsDialog() {
        JDialog dialog = new JDialog(parent, "🔔 Admin Notifications", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(AppTheme.BG_COLOR);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppTheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("🔔 Admin Notifications");
        headerLabel.setFont(AppTheme.FONT_HEADER);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(AppTheme.BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        try {
            List<Object[]> notifications = dbManager.getAllNotifications("admin");
            if (notifications.isEmpty()) {
                JLabel emptyLabel = new JLabel("No notifications yet.");
                emptyLabel.setFont(AppTheme.FONT_SUBTITLE);
                emptyLabel.setForeground(AppTheme.TEXT_SECONDARY);
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(Box.createVerticalGlue());
                contentPanel.add(emptyLabel);
                contentPanel.add(Box.createVerticalGlue());
            } else {
                for (Object[] n : notifications) {
                    JPanel notifCard = createNotificationCard(n);
                    contentPanel.add(notifCard);
                    contentPanel.add(Box.createVerticalStrut(10));
                }
            }
            
            // Mark all as read
            dbManager.markAllNotificationsAsRead("admin");
            
        } catch (SQLException e) {
            UIHelper.showErrorDialog(dialog, "Error: " + e.getMessage());
        }
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(AppTheme.BG_COLOR);
        JButton btnClose = UIHelper.createStyledButton("Close", new Color(149, 165, 166));
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.addActionListener(e -> {
            dialog.dispose();
            if (refreshCallback != null) refreshCallback.run();
        });
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    // ============== CONTACT DIALOG ==============
    
    private void showContactDialog(JDialog parentDialog, String name, String email, String phone) {
        JDialog dialog = new JDialog(parentDialog, "📞 Contact Information", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(parentDialog);
        dialog.getContentPane().setBackground(AppTheme.BG_COLOR);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 152, 219));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("📞 Contact Information");
        headerLabel.setFont(AppTheme.FONT_HEADER);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = UIHelper.createCardPanel(25);
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        addContactRow(contentPanel, "Name:", name);
        addContactRow(contentPanel, "Email:", email);
        addContactRow(contentPanel, "Phone:", phone != null && !phone.isEmpty() ? phone : "Not provided");
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        
        // Close button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(AppTheme.BG_COLOR);
        JButton btnClose = UIHelper.createStyledButton("Close", new Color(149, 165, 166));
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void addContactRow(JPanel panel, String label, String value) {
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
        panel.add(Box.createVerticalStrut(12));
    }
    
    // ============== CUSTOMER: APPLY FOR JOB ==============
    
    public void showApplyJobDialog(String jobTitle, String company) {
        JDialog dialog = new JDialog(parent, "📝 Apply for Job", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(AppTheme.BG_COLOR);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppTheme.ACCENT_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("📝 Job Application");
        headerLabel.setFont(AppTheme.FONT_HEADER);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Form
        JPanel formPanel = UIHelper.createCardPanel(25);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Job Info
        JLabel lblJobInfo = new JLabel("<html><b>" + jobTitle + "</b><br>" + company + "</html>");
        lblJobInfo.setFont(AppTheme.FONT_SUBTITLE);
        lblJobInfo.setForeground(AppTheme.TEXT_SECONDARY);
        lblJobInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblJobInfo);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Name
        formPanel.add(UIHelper.createFormLabel("Full Name *"));
        JTextField tfName = UIHelper.createStyledTextField(20);
        tfName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfName.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(tfName);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Email
        formPanel.add(UIHelper.createFormLabel("Email *"));
        JTextField tfEmail = UIHelper.createStyledTextField(20);
        tfEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(tfEmail);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Phone
        formPanel.add(UIHelper.createFormLabel("Phone Number"));
        JTextField tfPhone = UIHelper.createStyledTextField(20);
        tfPhone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfPhone.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(tfPhone);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Cover Letter
        formPanel.add(UIHelper.createFormLabel("Cover Letter"));
        JTextArea taCoverLetter = UIHelper.createStyledTextArea(4, 20);
        JScrollPane scrollPane = new JScrollPane(taCoverLetter);
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(scrollPane);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        btnPanel.setBackground(AppTheme.BG_COLOR);
        
        JButton btnSubmit = UIHelper.createStyledButton("📨 Submit Application", AppTheme.ACCENT_COLOR);
        btnSubmit.setPreferredSize(new Dimension(180, 40));
        btnSubmit.addActionListener(e -> {
            String name = tfName.getText().trim();
            String email = tfEmail.getText().trim();
            
            if (name.isEmpty() || email.isEmpty()) {
                UIHelper.showWarningDialog(dialog, "Name and Email are required.");
                return;
            }
            
            try {
                if (dbManager.addApplication(currentUser, jobTitle, company, name, email, 
                        tfPhone.getText().trim(), taCoverLetter.getText().trim())) {
                    UIHelper.showSuccessDialog(dialog, "Application submitted successfully! Admin has been notified and will review your application.");
                    dialog.dispose();
                    if (refreshCallback != null) refreshCallback.run();
                }
            } catch (SQLException ex) {
                UIHelper.showErrorDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        
        JButton btnCancel = UIHelper.createStyledButton("Cancel", new Color(149, 165, 166));
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.addActionListener(e -> dialog.dispose());
        
        btnPanel.add(btnSubmit);
        btnPanel.add(btnCancel);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    // ============== CUSTOMER: MY APPLICATIONS ==============
    
    public void showMyApplicationsDialog() {
        JDialog dialog = new JDialog(parent, "📋 My Applications", true);
        dialog.setSize(700, 400);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(AppTheme.BG_COLOR);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppTheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("📋 My Job Applications");
        headerLabel.setFont(AppTheme.FONT_HEADER);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Job Title", "Company", "Name", "Email", "Status", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable table = new JTable(model);
        table.setFont(AppTheme.FONT_TABLE);
        table.setRowHeight(35);
        
        // Status color renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                String status = String.valueOf(v);
                if ("Approved".equals(status)) {
                    l.setForeground(AppTheme.ACCENT_COLOR);
                } else if ("Rejected".equals(status)) {
                    l.setForeground(AppTheme.DANGER_COLOR);
                } else {
                    l.setForeground(AppTheme.WARNING_COLOR);
                }
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                return l;
            }
        });
        
        try {
            List<Object[]> apps = dbManager.getUserApplications(currentUser);
            for (Object[] app : apps) {
                model.addRow(app);
            }
        } catch (SQLException e) {
            UIHelper.showErrorDialog(dialog, "Error loading applications: " + e.getMessage());
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(AppTheme.BG_COLOR);
        JButton btnClose = UIHelper.createStyledButton("Close", new Color(149, 165, 166));
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    // ============== CUSTOMER: NOTIFICATIONS ==============
    
    public void showNotificationsDialog() {
        JDialog dialog = new JDialog(parent, "🔔 Notifications", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(AppTheme.BG_COLOR);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppTheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("🔔 My Notifications");
        headerLabel.setFont(AppTheme.FONT_HEADER);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Content Panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(AppTheme.BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        try {
            List<Object[]> notifications = dbManager.getAllNotifications(currentUser);
            if (notifications.isEmpty()) {
                JLabel emptyLabel = new JLabel("No notifications yet.");
                emptyLabel.setFont(AppTheme.FONT_SUBTITLE);
                emptyLabel.setForeground(AppTheme.TEXT_SECONDARY);
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                contentPanel.add(Box.createVerticalGlue());
                contentPanel.add(emptyLabel);
                contentPanel.add(Box.createVerticalGlue());
            } else {
                for (Object[] n : notifications) {
                    JPanel notifCard = createNotificationCard(n);
                    contentPanel.add(notifCard);
                    contentPanel.add(Box.createVerticalStrut(10));
                }
            }
            
            // Mark all as read
            dbManager.markAllNotificationsAsRead(currentUser);
            
        } catch (SQLException e) {
            UIHelper.showErrorDialog(dialog, "Error: " + e.getMessage());
        }
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(AppTheme.BG_COLOR);
        JButton btnClose = UIHelper.createStyledButton("Close", new Color(149, 165, 166));
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.addActionListener(e -> {
            dialog.dispose();
            if (refreshCallback != null) refreshCallback.run();
        });
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private JPanel createNotificationCard(Object[] notification) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(AppTheme.BORDER_COLOR, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        
        String status = String.valueOf(notification[3]);
        String icon = "Approved".equals(status) ? "✅" : "❌";
        Color statusColor = "Approved".equals(status) ? AppTheme.ACCENT_COLOR : AppTheme.DANGER_COLOR;
        
        // Left: Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        card.add(iconLabel, BorderLayout.WEST);
        
        // Center: Content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(String.valueOf(notification[1]));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        centerPanel.add(titleLabel);
        
        JLabel msgLabel = new JLabel(String.valueOf(notification[2]));
        msgLabel.setFont(AppTheme.FONT_SUBTITLE);
        msgLabel.setForeground(AppTheme.TEXT_SECONDARY);
        centerPanel.add(msgLabel);
        
        card.add(centerPanel, BorderLayout.CENTER);
        
        // Right: Status
        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(statusColor);
        card.add(statusLabel, BorderLayout.EAST);
        
        return card;
    }
    
    // ============== CUSTOMER: RESUME ==============
    
    public void showResumeDialog() {
        JDialog dialog = new JDialog(parent, "📄 My Resume", true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(AppTheme.BG_COLOR);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppTheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("📄 My Resume");
        headerLabel.setFont(AppTheme.FONT_HEADER);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Form
        JPanel formPanel = UIHelper.createCardPanel(25);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        
        // Full Name
        formPanel.add(UIHelper.createFormLabel("Full Name *"));
        JTextField tfFullName = UIHelper.createStyledTextField(20);
        tfFullName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfFullName.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(tfFullName);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Email
        formPanel.add(UIHelper.createFormLabel("Email *"));
        JTextField tfEmail = UIHelper.createStyledTextField(20);
        tfEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(tfEmail);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Phone
        formPanel.add(UIHelper.createFormLabel("Phone Number"));
        JTextField tfPhone = UIHelper.createStyledTextField(20);
        tfPhone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfPhone.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(tfPhone);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Address
        formPanel.add(UIHelper.createFormLabel("Address"));
        JTextArea taAddress = UIHelper.createStyledTextArea(2, 20);
        JScrollPane addressScroll = new JScrollPane(taAddress);
        addressScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        addressScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(addressScroll);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Education
        formPanel.add(UIHelper.createFormLabel("Education"));
        JTextArea taEducation = UIHelper.createStyledTextArea(3, 20);
        JScrollPane educationScroll = new JScrollPane(taEducation);
        educationScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        educationScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(educationScroll);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Experience
        formPanel.add(UIHelper.createFormLabel("Work Experience"));
        JTextArea taExperience = UIHelper.createStyledTextArea(3, 20);
        JScrollPane experienceScroll = new JScrollPane(taExperience);
        experienceScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        experienceScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(experienceScroll);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Skills
        formPanel.add(UIHelper.createFormLabel("Skills"));
        JTextArea taSkills = UIHelper.createStyledTextArea(2, 20);
        JScrollPane skillsScroll = new JScrollPane(taSkills);
        skillsScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        skillsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(skillsScroll);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Summary
        formPanel.add(UIHelper.createFormLabel("Professional Summary"));
        JTextArea taSummary = UIHelper.createStyledTextArea(3, 20);
        JScrollPane summaryScroll = new JScrollPane(taSummary);
        summaryScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        summaryScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(summaryScroll);
        
        // Load existing resume if available
        boolean resumeFilled = false;
        try {
            Object[] resume = dbManager.getResume(currentUser);
            if (resume != null) {
                String fullName = resume[0] != null ? String.valueOf(resume[0]) : "";
                String email = resume[1] != null ? String.valueOf(resume[1]) : "";
                tfFullName.setText(fullName);
                tfEmail.setText(email);
                tfPhone.setText(resume[2] != null ? String.valueOf(resume[2]) : "");
                taAddress.setText(resume[3] != null ? String.valueOf(resume[3]) : "");
                taEducation.setText(resume[4] != null ? String.valueOf(resume[4]) : "");
                taExperience.setText(resume[5] != null ? String.valueOf(resume[5]) : "");
                taSkills.setText(resume[6] != null ? String.valueOf(resume[6]) : "");
                taSummary.setText(resume[7] != null ? String.valueOf(resume[7]) : "");
                
                // Check if resume is filled (at least name and email)
                resumeFilled = !fullName.isEmpty() && !email.isEmpty();
            }
        } catch (SQLException e) {
            UIHelper.showErrorDialog(dialog, "Error loading resume: " + e.getMessage());
        }
        
        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(formScroll, BorderLayout.CENTER);
        
        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        btnPanel.setBackground(AppTheme.BG_COLOR);
        
        JButton btnSave = UIHelper.createStyledButton("💾 Save Resume", AppTheme.ACCENT_COLOR);
        btnSave.setPreferredSize(new Dimension(150, 40));
        btnSave.addActionListener(e -> {
            String fullName = tfFullName.getText().trim();
            String email = tfEmail.getText().trim();
            
            if (fullName.isEmpty() || email.isEmpty()) {
                UIHelper.showWarningDialog(dialog, "Full Name and Email are required.");
                return;
            }
            
            try {
                if (dbManager.saveOrUpdateResume(currentUser, fullName, email, tfPhone.getText().trim(),
                        taAddress.getText().trim(), taEducation.getText().trim(), taExperience.getText().trim(),
                        taSkills.getText().trim(), taSummary.getText().trim())) {
                    UIHelper.showSuccessDialog(dialog, "Resume saved successfully!");
                    // Refresh the dialog to show "Apply for Job" button if resume is now filled
                    dialog.dispose();
                    showResumeDialog(); // Reopen to show the new button
                }
            } catch (SQLException ex) {
                UIHelper.showErrorDialog(dialog, "Error saving resume: " + ex.getMessage());
            }
        });
        
        // Apply for Job button (only show if resume is filled)
        if (resumeFilled) {
            JButton btnApplyForJob = UIHelper.createStyledButton("📋 Apply for Job", AppTheme.PRIMARY_COLOR);
            btnApplyForJob.setPreferredSize(new Dimension(180, 40));
            btnApplyForJob.addActionListener(e -> {
                dialog.dispose();
                if (mainApp != null) {
                    mainApp.openJobListWindow();
                } else {
                    JOptionPane.showMessageDialog(parent, "Please open the Job List window to apply for jobs.", 
                        "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            btnPanel.add(btnApplyForJob);
        }
        
        JButton btnCancel = UIHelper.createStyledButton("Cancel", new Color(149, 165, 166));
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.addActionListener(e -> dialog.dispose());
        
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    // ============== ADMIN: CUSTOMER CONCERNS ==============
    
    public void showAdminContactMessagesDialog() {
        JDialog dialog = new JDialog(parent, "📧 Customer Concerns", true);
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(AppTheme.BG_COLOR);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppTheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("📧 Customer Concerns & Messages");
        headerLabel.setFont(AppTheme.FONT_HEADER);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        try {
            int unreadCount = dbManager.getUnreadContactMessageCount();
            if (unreadCount > 0) {
                JLabel countLabel = new JLabel("Unread: " + unreadCount);
                countLabel.setFont(AppTheme.FONT_SUBTITLE);
                countLabel.setForeground(AppTheme.DANGER_COLOR);
                countLabel.setForeground(new Color(255, 255, 255));
                headerPanel.add(countLabel, BorderLayout.EAST);
            }
        } catch (SQLException e) { }
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Username", "Subject", "Email", "Phone", "Status", "Date"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable table = new JTable(tableModel);
        table.setFont(AppTheme.FONT_TABLE);
        table.setRowHeight(35);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(AppTheme.FONT_TABLE_HEADER);
        header.setBackground(AppTheme.PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        
        try {
            List<Object[]> messages = dbManager.getAllContactMessages();
            for (Object[] msg : messages) {
                tableModel.addRow(new Object[]{
                    msg[0], // id
                    msg[1], // username
                    msg[2], // subject
                    msg[4], // email
                    msg[5], // phone
                    msg[6], // status
                    msg[9]  // created_at
                });
            }
        } catch (SQLException e) {
            UIHelper.showErrorDialog(dialog, "Error loading messages: " + e.getMessage());
        }
        
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR, 1));
        dialog.add(tableScroll, BorderLayout.CENTER);
        
        // Details and Response Panel
        JPanel detailsPanel = UIHelper.createCardPanel(15);
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        detailsPanel.setPreferredSize(new Dimension(0, 200));
        
        JLabel detailsLabel = new JLabel("Message Details:");
        detailsLabel.setFont(AppTheme.FONT_LABEL);
        detailsLabel.setForeground(AppTheme.TEXT_PRIMARY);
        detailsPanel.add(detailsLabel);
        detailsPanel.add(Box.createVerticalStrut(10));
        
        JTextArea messageArea = new JTextArea();
        messageArea.setFont(AppTheme.FONT_SUBTITLE);
        messageArea.setEditable(false);
        messageArea.setOpaque(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setForeground(AppTheme.TEXT_SECONDARY);
        
        JTextArea responseArea = new JTextArea(3, 30);
        responseArea.setFont(AppTheme.FONT_SUBTITLE);
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR, 1));
        
        JLabel responseLabel = new JLabel("Admin Response:");
        responseLabel.setFont(AppTheme.FONT_LABEL);
        responseLabel.setForeground(AppTheme.TEXT_PRIMARY);
        
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR, 1));
        messageScroll.setPreferredSize(new Dimension(0, 80));
        
        JScrollPane responseScroll = new JScrollPane(responseArea);
        responseScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR, 1));
        responseScroll.setPreferredSize(new Dimension(0, 60));
        
        detailsPanel.add(messageScroll);
        detailsPanel.add(Box.createVerticalStrut(10));
        detailsPanel.add(responseLabel);
        detailsPanel.add(Box.createVerticalStrut(5));
        detailsPanel.add(responseScroll);
        
        // Update details when row is selected
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                try {
                    int messageId = (Integer) tableModel.getValueAt(row, 0);
                    List<Object[]> allMessages = dbManager.getAllContactMessages();
                    for (Object[] msg : allMessages) {
                        if (((Integer) msg[0]) == messageId) {
                            String message = (String) msg[3];
                            String adminResponse = (String) msg[8];
                            
                            messageArea.setText("Message: " + message);
                            if (adminResponse != null && !adminResponse.trim().isEmpty()) {
                                responseArea.setText(adminResponse);
                            } else {
                                responseArea.setText("");
                            }
                            
                            // Mark as read
                            dbManager.markContactMessageAsRead(messageId);
                            break;
                        }
                    }
                } catch (SQLException ex) {
                    UIHelper.showErrorDialog(dialog, "Error: " + ex.getMessage());
                }
            }
        });
        
        dialog.add(detailsPanel, BorderLayout.SOUTH);
        
        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        btnPanel.setBackground(AppTheme.BG_COLOR);
        
        JButton btnRespond = UIHelper.createStyledButton("✅ Respond & Mark as Resolved", new Color(46, 125, 50));
        btnRespond.setPreferredSize(new Dimension(220, 40));
        btnRespond.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                UIHelper.showWarningDialog(dialog, "Please select a message first.");
                return;
            }
            
            String response = responseArea.getText().trim();
            if (response.isEmpty()) {
                UIHelper.showWarningDialog(dialog, "Please enter a response.");
                return;
            }
            
            try {
                int messageId = (Integer) tableModel.getValueAt(row, 0);
                if (dbManager.updateContactMessageStatus(messageId, "Resolved", response)) {
                    UIHelper.showSuccessDialog(dialog, "Response sent successfully!");
                    responseArea.setText("");
                    // Refresh table
                    tableModel.setRowCount(0);
                    List<Object[]> messages = dbManager.getAllContactMessages();
                    for (Object[] msg : messages) {
                        tableModel.addRow(new Object[]{
                            msg[0], msg[1], msg[2], msg[4], msg[5], msg[6], msg[9]
                        });
                    }
                }
            } catch (SQLException ex) {
                UIHelper.showErrorDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        
        JButton btnClose = UIHelper.createStyledButton("Close", AppTheme.PRIMARY_COLOR);
        btnClose.setPreferredSize(new Dimension(100, 40));
        btnClose.addActionListener(e -> dialog.dispose());
        
        btnPanel.add(btnRespond);
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    // ============== ADMIN: USER MANAGEMENT ==============
    
    public void showUserManagementDialog() {
        JDialog dialog = new JDialog(parent, "👥 User Management", true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(AppTheme.BG_COLOR);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppTheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("👥 User Management - All Users");
        headerLabel.setFont(AppTheme.FONT_HEADER);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        try {
            int activeCount = dbManager.getActiveUserCount();
            JLabel countLabel = new JLabel("Active: " + activeCount);
            countLabel.setFont(AppTheme.FONT_SUBTITLE);
            countLabel.setForeground(new Color(255, 255, 255));
            headerPanel.add(countLabel, BorderLayout.EAST);
        } catch (SQLException e) { }
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"ID", "Username", "Password", "Role", "Status", "Last Login", "Last Logout"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable table = new JTable(tableModel);
        table.setFont(AppTheme.FONT_TABLE);
        table.setRowHeight(35);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(AppTheme.FONT_TABLE_HEADER);
        header.setBackground(AppTheme.PRIMARY_COLOR);
        header.setForeground(Color.WHITE);
        
        // Custom renderer for Password column (column 2)
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                String password = value != null ? value.toString() : "";
                label.setText("🔑 " + password);
                label.setForeground(new Color(128, 0, 128)); // Purple color for password
                label.setHorizontalAlignment(JLabel.LEFT);
                return label;
            }
        });
        
        // Custom renderer for Role column (now column 3)
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                String role = value != null ? value.toString() : "";
                if ("Admin".equals(role)) {
                    label.setForeground(AppTheme.PRIMARY_COLOR);
                    label.setText("🔐 " + role);
                } else {
                    label.setForeground(AppTheme.TEXT_SECONDARY);
                    label.setText("👤 " + role);
                }
                label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
        });
        
        // Custom renderer for Status column (now column 4)
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "Offline";
                if ("Online".equals(status)) {
                    label.setForeground(new Color(46, 125, 50)); // Green
                    label.setText("🟢 " + status);
                } else {
                    label.setForeground(AppTheme.TEXT_SECONDARY);
                    label.setText("⚫ " + status);
                }
                label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
        });
        
        // Custom renderer for Last Login column (now column 5)
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                String timestamp = value != null ? value.toString() : "Never";
                if (!"Never".equals(timestamp)) {
                    label.setText("🕐 " + timestamp);
                    label.setForeground(AppTheme.TEXT_PRIMARY);
                } else {
                    label.setText("Never");
                    label.setForeground(AppTheme.TEXT_SECONDARY);
                }
                label.setHorizontalAlignment(JLabel.LEFT);
                return label;
            }
        });
        
        // Custom renderer for Last Logout column (now column 6)
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                String timestamp = value != null ? value.toString() : "N/A";
                if (!"N/A".equals(timestamp) && !timestamp.isEmpty()) {
                    label.setText("🕐 " + timestamp);
                    label.setForeground(AppTheme.TEXT_PRIMARY);
                } else {
                    label.setText("N/A");
                    label.setForeground(AppTheme.TEXT_SECONDARY);
                }
                label.setHorizontalAlignment(JLabel.LEFT);
                return label;
            }
        });
        
        // Load users
        try {
            List<Object[]> users = dbManager.getAllUsersWithSessions();
            for (Object[] user : users) {
                String lastLogout = user[6] != null ? user[6].toString() : "N/A";
                tableModel.addRow(new Object[]{
                    user[0], // id
                    user[1], // username
                    user[2], // password
                    user[3], // role
                    user[4], // status
                    user[5], // last_login
                    lastLogout
                });
            }
        } catch (SQLException e) {
            UIHelper.showErrorDialog(dialog, "Error loading users: " + e.getMessage());
        }
        
        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Username
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Password
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // Role
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Status
        table.getColumnModel().getColumn(5).setPreferredWidth(200); // Last Login - wider for date+time
        table.getColumnModel().getColumn(6).setPreferredWidth(200); // Last Logout - wider for date+time
        
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR, 1));
        dialog.add(tableScroll, BorderLayout.CENTER);
        
        // Info Panel
        JPanel infoPanel = UIHelper.createCardPanel(15);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        infoPanel.setPreferredSize(new Dimension(0, 100));
        
        JLabel infoLabel = new JLabel("<html><b>Legend:</b><br>" +
            "🟢 Online - User is currently logged in<br>" +
            "⚫ Offline - User is not logged in<br>" +
            "🔐 Admin - Administrator account<br>" +
            "👤 Customer - Regular user account</html>");
        infoLabel.setFont(AppTheme.FONT_SUBTITLE);
        infoLabel.setForeground(AppTheme.TEXT_SECONDARY);
        infoPanel.add(infoLabel);
        
        dialog.add(infoPanel, BorderLayout.SOUTH);
        
        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPanel.setBackground(AppTheme.BG_COLOR);
        btnPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JButton btnRefresh = UIHelper.createStyledButton("🔄 Refresh", AppTheme.PRIMARY_COLOR);
        btnRefresh.setPreferredSize(new Dimension(120, 35));
        btnRefresh.addActionListener(e -> {
            // Refresh table
            tableModel.setRowCount(0);
            try {
                List<Object[]> users = dbManager.getAllUsersWithSessions();
                for (Object[] user : users) {
                    String lastLogout = user[6] != null ? user[6].toString() : "N/A";
                    tableModel.addRow(new Object[]{
                        user[0], user[1], user[2], user[3], user[4], user[5], lastLogout
                    });
                }
                // Update active count
                int activeCount = dbManager.getActiveUserCount();
                headerPanel.removeAll();
                headerPanel.add(headerLabel, BorderLayout.WEST);
                JLabel countLabel = new JLabel("Active: " + activeCount);
                countLabel.setFont(AppTheme.FONT_SUBTITLE);
                countLabel.setForeground(new Color(255, 255, 255));
                headerPanel.add(countLabel, BorderLayout.EAST);
                headerPanel.revalidate();
                headerPanel.repaint();
            } catch (SQLException ex) {
                UIHelper.showErrorDialog(dialog, "Error refreshing: " + ex.getMessage());
            }
        });
        
        JButton btnDelete = UIHelper.createStyledButton("🗑️ Delete User", AppTheme.DANGER_COLOR);
        btnDelete.setPreferredSize(new Dimension(140, 35));
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                UIHelper.showWarningDialog(dialog, "Please select a user to delete.");
                return;
            }
            
            int userId = (Integer) tableModel.getValueAt(row, 0);
            String username = String.valueOf(tableModel.getValueAt(row, 1));
            String role = String.valueOf(tableModel.getValueAt(row, 3));
            
            // Prevent deleting admin
            if ("Admin".equals(role) && "admin".equalsIgnoreCase(username)) {
                UIHelper.showErrorDialog(dialog, "Cannot delete the admin user.");
                return;
            }
            
            // Confirmation dialog
            int confirm = JOptionPane.showConfirmDialog(
                dialog,
                "Are you sure you want to delete user '" + username + "'?\n\n" +
                "This action cannot be undone and will delete:\n" +
                "- User account\n" +
                "- All user sessions\n" +
                "- All user applications\n" +
                "- All user resumes\n" +
                "- All user notifications",
                "Confirm Delete User",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (dbManager.deleteUser(userId, username)) {
                        UIHelper.showSuccessDialog(dialog, "User '" + username + "' has been deleted successfully.");
                        // Refresh table
                        tableModel.setRowCount(0);
                        List<Object[]> users = dbManager.getAllUsersWithSessions();
                        for (Object[] user : users) {
                            String lastLogout = user[6] != null ? user[6].toString() : "N/A";
                            tableModel.addRow(new Object[]{
                                user[0], user[1], user[2], user[3], user[4], user[5], lastLogout
                            });
                        }
                        // Update active count
                        int activeCount = dbManager.getActiveUserCount();
                        headerPanel.removeAll();
                        headerPanel.add(headerLabel, BorderLayout.WEST);
                        JLabel countLabel = new JLabel("Active: " + activeCount);
                        countLabel.setFont(AppTheme.FONT_SUBTITLE);
                        countLabel.setForeground(new Color(255, 255, 255));
                        headerPanel.add(countLabel, BorderLayout.EAST);
                        headerPanel.revalidate();
                        headerPanel.repaint();
                    } else {
                        UIHelper.showErrorDialog(dialog, "Failed to delete user.");
                    }
                } catch (SQLException ex) {
                    UIHelper.showErrorDialog(dialog, "Error deleting user: " + ex.getMessage());
                }
            }
        });
        
        JButton btnExport = UIHelper.createStyledButton("📥 Export to CSV", new Color(46, 125, 50));
        btnExport.setPreferredSize(new Dimension(150, 35));
        btnExport.addActionListener(e -> exportUsersToCSV(dialog, tableModel));
        
        JButton btnClose = UIHelper.createStyledButton("Close", AppTheme.PRIMARY_COLOR);
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.addActionListener(e -> dialog.dispose());
        
        btnPanel.add(btnRefresh);
        btnPanel.add(btnDelete);
        btnPanel.add(btnExport);
        btnPanel.add(btnClose);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void exportUsersToCSV(JDialog parent, DefaultTableModel tableModel) {
        try {
            // Ask user what to export
            String[] options = {"Export Visible Users (Table)", "Export All Users from Database"};
            int choice = JOptionPane.showOptionDialog(parent,
                "What would you like to export?",
                "Export Options",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
            
            boolean exportAll = (choice == 1);
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Users to CSV");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String defaultFileName = exportAll ? "all_users_export_" : "users_export_";
            fileChooser.setSelectedFile(new java.io.File(defaultFileName + sdf.format(new Date()) + ".csv"));
            
            if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new java.io.File(file.getAbsolutePath() + ".csv");
                }
                
                try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                    // Write UTF-8 BOM for Excel compatibility
                    writer.write('\ufeff');
                    
                    // Write header
                    String[] headers = {"ID", "Username", "Password", "Role", "Status", "Last Login", "Last Logout"};
                    for (int i = 0; i < headers.length; i++) {
                        writer.append(escapeCSV(headers[i]));
                        if (i < headers.length - 1) writer.append(",");
                    }
                    writer.append("\n");
                    
                    if (exportAll) {
                        // Export all users from database
                        List<Object[]> allUsers = dbManager.getAllUsersWithSessions();
                        for (Object[] user : allUsers) {
                            String lastLogout = user[6] != null ? user[6].toString() : "N/A";
                            writer.append(escapeCSV(String.valueOf(user[0]))).append(","); // ID
                            writer.append(escapeCSV(user[1].toString())).append(","); // Username
                            writer.append(escapeCSV(user[2].toString())).append(","); // Password
                            writer.append(escapeCSV(user[3].toString())).append(","); // Role
                            writer.append(escapeCSV(user[4].toString())).append(","); // Status
                            writer.append(escapeCSV(user[5].toString())).append(","); // Last Login
                            writer.append(escapeCSV(lastLogout)); // Last Logout
                            writer.append("\n");
                        }
                    } else {
                        // Export visible users from table
                        for (int row = 0; row < tableModel.getRowCount(); row++) {
                            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                                Object value = tableModel.getValueAt(row, col);
                                String strValue = value != null ? value.toString() : "";
                                writer.append(escapeCSV(strValue));
                                if (col < tableModel.getColumnCount() - 1) writer.append(",");
                            }
                            writer.append("\n");
                        }
                    }
                    
                    String message = exportAll ? 
                        "All users exported successfully!\nTotal: " + dbManager.getAllUsersWithSessions().size() + " users\n" :
                        "Visible users exported successfully!\nTotal: " + tableModel.getRowCount() + " users\n";
                    UIHelper.showSuccessDialog(parent, message + "Saved to:\n" + file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            UIHelper.showErrorDialog(parent, "Error exporting to CSV: " + e.getMessage());
        } catch (SQLException e) {
            UIHelper.showErrorDialog(parent, "Error loading users from database: " + e.getMessage());
        }
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "\"\"";
        
        // Remove emoji/icons from status and role for cleaner CSV
        String cleaned = value.replace("🟢 ", "").replace("⚫ ", "")
                             .replace("🔐 ", "").replace("👤 ", "")
                             .replace("🕐 ", "");
        
        // Replace commas with semicolons to avoid CSV issues
        String escaped = cleaned.replace(",", ";");
        
        // If value contains quotes, newlines, or commas, wrap in quotes
        if (escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            escaped = escaped.replace("\"", "\"\""); // Escape quotes
            return "\"" + escaped + "\"";
        }
        
        // Simple values don't need quotes unless they contain special chars
        if (escaped.contains(";") || escaped.trim().isEmpty()) {
            return "\"" + escaped + "\"";
        }
        
        return escaped;
    }
}
