import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * ContactUsFrame.java - Contact Us Form for Customer Concerns
 * 
 * Allows customers to send messages/concerns to the admin.
 */
public class ContactUsFrame extends JFrame {
    
    private DatabaseManager dbManager;
    private String currentUser;
    private JTextField tfSubject;
    private JTextField tfEmail;
    private JTextField tfPhone;
    private JTextArea taMessage;
    private JButton btnSend;
    private JButton btnViewMessages;
    
    public ContactUsFrame(JobListingApp parent, DatabaseManager dbManager, String currentUser) {
        super("Contact Us");
        this.dbManager = dbManager;
        this.currentUser = currentUser;
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 650);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(AppTheme.BG_COLOR);
        
        initializeComponents();
        setVisible(true);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        
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
        
        JLabel titleLabel = new JLabel("📧 Contact Us");
        titleLabel.setFont(AppTheme.FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton btnClose = UIHelper.createStyledButton("✖ Close", AppTheme.DANGER_COLOR);
        btnClose.setPreferredSize(new Dimension(90, 32));
        btnClose.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 14));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnClose);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        JPanel formCard = UIHelper.createCardPanel(25);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        // Info label
        JLabel infoLabel = new JLabel("<html><center>Have a question or concern?<br>Send us a message and we'll get back to you soon!</center></html>");
        infoLabel.setFont(AppTheme.FONT_SUBTITLE);
        infoLabel.setForeground(AppTheme.TEXT_SECONDARY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(infoLabel);
        formCard.add(Box.createVerticalStrut(25));
        
        // Subject
        formCard.add(UIHelper.createFormLabel("Subject *"));
        tfSubject = UIHelper.createStyledTextField(30);
        tfSubject.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfSubject.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(tfSubject);
        formCard.add(Box.createVerticalStrut(15));
        
        // Email
        formCard.add(UIHelper.createFormLabel("Email *"));
        tfEmail = UIHelper.createStyledTextField(30);
        tfEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(tfEmail);
        formCard.add(Box.createVerticalStrut(15));
        
        // Phone (Optional)
        formCard.add(UIHelper.createFormLabel("Phone (Optional)"));
        tfPhone = UIHelper.createStyledTextField(30);
        tfPhone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfPhone.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(tfPhone);
        formCard.add(Box.createVerticalStrut(15));
        
        // Message
        formCard.add(UIHelper.createFormLabel("Message *"));
        taMessage = UIHelper.createStyledTextArea(8, 30);
        taMessage.setLineWrap(true);
        taMessage.setWrapStyleWord(true);
        JScrollPane messageScroll = new JScrollPane(taMessage);
        messageScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        messageScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        messageScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR, 1));
        formCard.add(messageScroll);
        
        JScrollPane formScroll = new JScrollPane(formCard);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(formScroll, BorderLayout.CENTER);
        
        return contentPanel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        footerPanel.setBackground(AppTheme.BG_COLOR);
        footerPanel.setBorder(new EmptyBorder(10, 20, 15, 20));
        
        btnViewMessages = UIHelper.createStyledButton("📋 View My Messages", AppTheme.PRIMARY_COLOR);
        btnViewMessages.setPreferredSize(new Dimension(180, 40));
        btnViewMessages.addActionListener(e -> showUserMessages());
        footerPanel.add(btnViewMessages);
        
        btnSend = UIHelper.createStyledButton("📤 Send Message", AppTheme.ACCENT_COLOR);
        btnSend.setPreferredSize(new Dimension(150, 40));
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSend.addActionListener(e -> sendMessage());
        footerPanel.add(btnSend);
        
        return footerPanel;
    }
    
    private void sendMessage() {
        String subject = tfSubject.getText().trim();
        String email = tfEmail.getText().trim();
        String phone = tfPhone.getText().trim();
        String message = taMessage.getText().trim();
        
        // Validation
        if (subject.isEmpty()) {
            UIHelper.showWarningDialog(this, "Please enter a subject.");
            tfSubject.requestFocus();
            return;
        }
        
        if (email.isEmpty()) {
            UIHelper.showWarningDialog(this, "Please enter your email address.");
            tfEmail.requestFocus();
            return;
        }
        
        // Basic email validation
        if (!email.contains("@") || !email.contains(".")) {
            UIHelper.showWarningDialog(this, "Please enter a valid email address.");
            tfEmail.requestFocus();
            return;
        }
        
        if (message.isEmpty()) {
            UIHelper.showWarningDialog(this, "Please enter your message.");
            taMessage.requestFocus();
            return;
        }
        
        if (message.length() < 10) {
            UIHelper.showWarningDialog(this, "Message is too short. Please provide more details.");
            taMessage.requestFocus();
            return;
        }
        
        try {
            if (dbManager.addContactMessage(currentUser, subject, message, email, phone)) {
                UIHelper.showSuccessDialog(this, "Message sent successfully!\nWe'll get back to you soon.");
                
                // Clear form
                tfSubject.setText("");
                tfEmail.setText("");
                tfPhone.setText("");
                taMessage.setText("");
            } else {
                UIHelper.showErrorDialog(this, "Failed to send message. Please try again.");
            }
        } catch (SQLException e) {
            UIHelper.showErrorDialog(this, "Error sending message: " + e.getMessage());
        }
    }
    
    private void showUserMessages() {
        try {
            java.util.List<Object[]> messages = dbManager.getUserContactMessages(currentUser);
            
            if (messages.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You haven't sent any messages yet.", 
                    "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // Create dialog to show messages
            JDialog messagesDialog = new JDialog(this, "My Messages", true);
            messagesDialog.setSize(700, 500);
            messagesDialog.setLocationRelativeTo(this);
            messagesDialog.getContentPane().setBackground(AppTheme.BG_COLOR);
            messagesDialog.setLayout(new BorderLayout(10, 10));
            
            // Header
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(AppTheme.PRIMARY_COLOR);
            headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
            JLabel headerLabel = new JLabel("📋 My Contact Messages");
            headerLabel.setFont(AppTheme.FONT_HEADER);
            headerLabel.setForeground(Color.WHITE);
            headerPanel.add(headerLabel, BorderLayout.WEST);
            messagesDialog.add(headerPanel, BorderLayout.NORTH);
            
            // Content
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setOpaque(false);
            contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
            
            DefaultListModel<String> listModel = new DefaultListModel<>();
            JList<String> messagesList = new JList<>(listModel);
            messagesList.setFont(AppTheme.FONT_SUBTITLE);
            messagesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            messagesList.setCellRenderer(new DefaultListCellRenderer() {
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                        boolean isSelected, boolean cellHasFocus) {
                    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    label.setBorder(new EmptyBorder(8, 10, 8, 10));
                    return label;
                }
            });
            
            // Populate list
            for (Object[] msg : messages) {
                String subject = (String) msg[1];
                String status = (String) msg[3];
                String date = (String) msg[5];
                
                String statusIcon = "New".equals(status) ? "🆕" : "✅";
                String displayText = String.format("%s [%s] %s - %s", statusIcon, status, subject, date);
                listModel.addElement(displayText);
            }
            
            JScrollPane listScroll = new JScrollPane(messagesList);
            listScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR, 1));
            contentPanel.add(listScroll, BorderLayout.CENTER);
            
            // Details panel
            JPanel detailsPanel = UIHelper.createCardPanel(15);
            detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
            detailsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
            detailsPanel.setPreferredSize(new Dimension(0, 150));
            
            JLabel detailsLabel = new JLabel("Message Details:");
            detailsLabel.setFont(AppTheme.FONT_LABEL);
            detailsLabel.setForeground(AppTheme.TEXT_PRIMARY);
            detailsPanel.add(detailsLabel);
            detailsPanel.add(Box.createVerticalStrut(10));
            
            JTextArea detailsArea = new JTextArea();
            detailsArea.setFont(AppTheme.FONT_SUBTITLE);
            detailsArea.setEditable(false);
            detailsArea.setOpaque(false);
            detailsArea.setLineWrap(true);
            detailsArea.setWrapStyleWord(true);
            detailsArea.setForeground(AppTheme.TEXT_SECONDARY);
            
            messagesList.addListSelectionListener(e -> {
                int selectedIndex = messagesList.getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < messages.size()) {
                    Object[] msg = messages.get(selectedIndex);
                    String subject = (String) msg[1];
                    String message = (String) msg[2];
                    String status = (String) msg[3];
                    String adminResponse = (String) msg[4];
                    String date = (String) msg[5];
                    
                    StringBuilder details = new StringBuilder();
                    details.append("Subject: ").append(subject).append("\n\n");
                    details.append("Your Message:\n").append(message).append("\n\n");
                    details.append("Status: ").append(status).append("\n");
                    details.append("Sent: ").append(date);
                    
                    if (adminResponse != null && !adminResponse.trim().isEmpty()) {
                        details.append("\n\n").append("Admin Response:\n").append(adminResponse);
                    }
                    
                    detailsArea.setText(details.toString());
                }
            });
            
            JScrollPane detailsScroll = new JScrollPane(detailsArea);
            detailsScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_COLOR, 1));
            detailsScroll.setPreferredSize(new Dimension(0, 100));
            detailsPanel.add(detailsScroll);
            
            contentPanel.add(detailsPanel, BorderLayout.SOUTH);
            messagesDialog.add(contentPanel, BorderLayout.CENTER);
            
            // Close button
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            btnPanel.setOpaque(false);
            JButton btnClose = UIHelper.createStyledButton("Close", AppTheme.PRIMARY_COLOR);
            btnClose.addActionListener(e -> messagesDialog.dispose());
            btnPanel.add(btnClose);
            messagesDialog.add(btnPanel, BorderLayout.SOUTH);
            
            messagesDialog.setVisible(true);
            
        } catch (SQLException e) {
            UIHelper.showErrorDialog(this, "Error loading messages: " + e.getMessage());
        }
    }
    
}

