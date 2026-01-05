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
 * JobListFrame.java - Separate JFrame for Job Listing Table
 * 
 * This frame displays the job listing table with search functionality.
 */
public class JobListFrame extends JFrame {
    
    // Components
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField tfSearch;
    private JButton btnDelete;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel jobCount;
    
    // Managers
    private DatabaseManager dbManager;
    private DialogManager dialogManager;
    
    // User Session
    @SuppressWarnings("unused")
    private String currentUser;
    private String currentUserRole;
    private JobListingApp parentApp;
    
    public JobListFrame(JobListingApp parent, DatabaseManager dbManager, DialogManager dialogManager, 
                       String currentUser, String currentUserRole) {
        super("Job Listings");
        this.parentApp = parent;
        this.dbManager = dbManager;
        this.dialogManager = dialogManager;
        this.currentUser = currentUser;
        this.currentUserRole = currentUserRole;
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(AppTheme.BG_COLOR);
        
        initializeComponents();
        loadJobsFromDatabase();
        
        setVisible(true);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Content
        add(createContentPanel(), BorderLayout.CENTER);
        
        // Footer
        add(createFooterPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppTheme.PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
        
        JLabel titleLabel = new JLabel("📋 Job Listings");
        titleLabel.setFont(AppTheme.FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton btnRefresh = UIHelper.createStyledButton("🔄 Refresh", AppTheme.ACCENT_COLOR);
        btnRefresh.setPreferredSize(new Dimension(100, 32));
        btnRefresh.addActionListener(e -> loadJobsFromDatabase());
        
        JButton btnClose = UIHelper.createStyledButton("✖ Close", AppTheme.DANGER_COLOR);
        btnClose.setPreferredSize(new Dimension(90, 32));
        btnClose.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnClose);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
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
        
        jobCount = new JLabel("Total Jobs: 0");
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
        
        // Custom renderer for ID column (sequential numbers)
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setText(String.valueOf(r + 1));
                l.setBackground(s ? new Color(52, 152, 219, 100) : (r % 2 == 0 ? Color.WHITE : new Color(240, 248, 255)));
                l.setForeground(Color.BLACK);
                l.setFont(AppTheme.FONT_TABLE);
                l.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                l.setHorizontalAlignment(JLabel.CENTER);
                l.setOpaque(true);
                return l;
            }
        });
        
        // Cell renderer for other columns
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setBackground(s ? new Color(52, 152, 219, 100) : (r % 2 == 0 ? Color.WHITE : new Color(240, 248, 255)));
                l.setForeground(Color.BLACK);
                l.setFont(AppTheme.FONT_TABLE);
                l.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                l.setHorizontalAlignment(JLabel.LEFT);
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
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        footerPanel.setBackground(AppTheme.BG_COLOR);
        footerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        // Add New Job button (Admin only)
        if ("Admin".equals(currentUserRole)) {
            JButton btnAddNewJob = UIHelper.createStyledButton("➕ Add New Job", AppTheme.ACCENT_COLOR);
            btnAddNewJob.setPreferredSize(new Dimension(150, 35));
            btnAddNewJob.addActionListener(e -> showAddJobWindow());
            footerPanel.add(btnAddNewJob);
        }
        
        btnDelete = UIHelper.createStyledButton("🗑️ Delete Selected Job", AppTheme.DANGER_COLOR);
        btnDelete.setPreferredSize(new Dimension(180, 35));
        btnDelete.addActionListener(e -> deleteSelectedJob());
        if ("Admin".equals(currentUserRole)) {
            footerPanel.add(btnDelete);
        }
        
        // Export to CSV button (Admin only)
        if ("Admin".equals(currentUserRole)) {
            JButton btnExport = UIHelper.createStyledButton("📥 Export to CSV", new Color(46, 125, 50));
            btnExport.setPreferredSize(new Dimension(150, 35));
            btnExport.addActionListener(e -> exportToCSV());
            footerPanel.add(btnExport);
        }
        
        // Apply with Resume button (Customer only)
        if (!"Admin".equals(currentUserRole)) {
            JButton btnApplyWithResume = UIHelper.createStyledButton("📝 Apply with Resume", AppTheme.PRIMARY_COLOR);
            btnApplyWithResume.setPreferredSize(new Dimension(180, 35));
            btnApplyWithResume.addActionListener(e -> applyWithResume());
            footerPanel.add(btnApplyWithResume);
        }
        
        return footerPanel;
    }
    
    public void loadJobsFromDatabase() {
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
    
    private void deleteSelectedJob() {
        int row = table.getSelectedRow();
        if (row == -1) { 
            UIHelper.showWarningDialog(this, "Please select a job."); 
            return; 
        }
        
        if (UIHelper.showConfirmDialog(this, "Delete this job?", "Confirm") == JOptionPane.YES_OPTION) {
            int modelRow = table.convertRowIndexToModel(row);
            int jobId = Integer.parseInt(String.valueOf(tableModel.getValueAt(modelRow, 0)));
            
            try {
                if (dbManager.deleteJob(jobId)) {
                    tableModel.removeRow(modelRow);
                    table.clearSelection();
                    UIHelper.showSuccessDialog(this, "Job deleted!");
                    loadJobsFromDatabase();
                    // Notify parent to refresh
                    if (parentApp != null) {
                        parentApp.refreshJobList();
                    }
                }
            } catch (SQLException ex) { 
                UIHelper.showErrorDialog(this, "Error: " + ex.getMessage()); 
            }
        }
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
            deleteItem.addActionListener(e -> deleteSelectedJob());
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
        
        // Export to CSV (Admin only)
        if ("Admin".equals(currentUserRole)) {
            contextMenu.addSeparator();
            JMenuItem exportItem = new JMenuItem("📥 Export to CSV");
            exportItem.addActionListener(e -> exportToCSV());
            contextMenu.add(exportItem);
        }
        
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
    
    // Getter methods for parent app to access
    public JTable getTable() {
        return table;
    }
    
    public DefaultTableModel getTableModel() {
        return tableModel;
    }
    
    // ============== APPLY WITH RESUME ==============
    
    private void applyWithResume() {
        int row = table.getSelectedRow();
        if (row == -1) {
            UIHelper.showWarningDialog(this, "Please select a job first.");
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(row);
        String jobTitle = String.valueOf(tableModel.getValueAt(modelRow, 1));
        String company = String.valueOf(tableModel.getValueAt(modelRow, 2));
        
        // Check if resume exists
        try {
            Object[] resume = dbManager.getResume(currentUser);
            if (resume == null || resume[0] == null || String.valueOf(resume[0]).trim().isEmpty() ||
                resume[1] == null || String.valueOf(resume[1]).trim().isEmpty()) {
                // No resume or incomplete resume - open resume dialog
                JOptionPane.showMessageDialog(this, 
                    "Please fill up your resume first to apply for jobs.\nOpening Resume dialog...", 
                    "Resume Required", JOptionPane.INFORMATION_MESSAGE);
                if (dialogManager != null) {
                    dialogManager.showResumeDialog();
                }
            } else {
                // Resume exists - show apply dialog with resume info pre-filled
                if (dialogManager != null) {
                    dialogManager.showApplyJobDialog(jobTitle, company);
                }
            }
        } catch (SQLException e) {
            UIHelper.showErrorDialog(this, "Error checking resume: " + e.getMessage());
        }
    }
    
    // ============== ADD NEW JOB WINDOW ==============
    
    private void showAddJobWindow() {
        JFrame addJobFrame = new JFrame("➕ Add New Job");
        addJobFrame.setSize(500, 600);
        addJobFrame.setLocationRelativeTo(this);
        addJobFrame.getContentPane().setBackground(AppTheme.BG_COLOR);
        addJobFrame.setLayout(new BorderLayout(10, 10));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AppTheme.ACCENT_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel headerLabel = new JLabel("➕ Add New Job");
        headerLabel.setFont(AppTheme.FONT_HEADER);
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        addJobFrame.add(headerPanel, BorderLayout.NORTH);
        
        // Form Panel
        JPanel formPanel = UIHelper.createCardPanel(25);
        formPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        
        // Title
        formPanel.add(UIHelper.createFormLabel("Job Title *"));
        JTextField tfTitle = UIHelper.createStyledTextField(20);
        tfTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(tfTitle);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Company
        formPanel.add(UIHelper.createFormLabel("Company *"));
        JTextField tfCompany = UIHelper.createStyledTextField(20);
        tfCompany.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfCompany.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(tfCompany);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Location
        formPanel.add(UIHelper.createFormLabel("Location *"));
        JTextField tfLocation = UIHelper.createStyledTextField(20);
        tfLocation.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfLocation.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(tfLocation);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Salary
        formPanel.add(UIHelper.createFormLabel("Salary *"));
        JTextField tfSalary = UIHelper.createStyledTextField(20);
        tfSalary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfSalary.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(tfSalary);
        formPanel.add(Box.createVerticalStrut(12));
        
        // Description
        formPanel.add(UIHelper.createFormLabel("Description *"));
        JTextArea taDescription = UIHelper.createStyledTextArea(5, 20);
        JScrollPane descScroll = new JScrollPane(taDescription);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(descScroll);
        
        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        addJobFrame.add(formScroll, BorderLayout.CENTER);
        
        // Button Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        btnPanel.setBackground(AppTheme.BG_COLOR);
        
        JButton btnAddJob = UIHelper.createStyledButton("➕ Add Job", AppTheme.ACCENT_COLOR);
        btnAddJob.setPreferredSize(new Dimension(150, 40));
        btnAddJob.addActionListener(e -> {
            String title = tfTitle.getText().trim();
            String company = tfCompany.getText().trim();
            String location = tfLocation.getText().trim();
            String salary = tfSalary.getText().trim();
            String description = taDescription.getText().trim();
            
            if (title.isEmpty() || company.isEmpty() || location.isEmpty() || 
                salary.isEmpty() || description.isEmpty()) {
                UIHelper.showWarningDialog(addJobFrame, "Please fill in all required fields.");
                return;
            }
            
            try {
                int newId = dbManager.addJob(title, company, location, salary, description);
                if (newId > 0) {
                    UIHelper.showSuccessDialog(addJobFrame, "Job added successfully!");
                    tfTitle.setText("");
                    tfCompany.setText("");
                    tfLocation.setText("");
                    tfSalary.setText("");
                    taDescription.setText("");
                    loadJobsFromDatabase(); // Refresh the job list
                    if (parentApp != null) {
                        parentApp.refreshJobList(); // Also refresh parent if needed
                    }
                    addJobFrame.dispose();
                } else {
                    UIHelper.showErrorDialog(addJobFrame, "Failed to add job.");
                }
            } catch (SQLException ex) {
                UIHelper.showErrorDialog(addJobFrame, "Error adding job: " + ex.getMessage());
            }
        });
        
        JButton btnCancel = UIHelper.createStyledButton("Cancel", new Color(149, 165, 166));
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.addActionListener(e -> addJobFrame.dispose());
        
        btnPanel.add(btnAddJob);
        btnPanel.add(btnCancel);
        addJobFrame.add(btnPanel, BorderLayout.SOUTH);
        
        addJobFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addJobFrame.setVisible(true);
    }
}

