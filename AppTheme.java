import java.awt.*;

/**
 * AppTheme.java - Colors and Fonts
 * 
 * Centralized theme configuration for the Job Listing System.
 */
public class AppTheme {
    
    // Colors
    public static final Color BG_COLOR = new Color(245, 247, 250);
    public static final Color PRIMARY_COLOR = new Color(33, 150, 243); // Blue
    public static final Color PRIMARY_DARK = new Color(25, 118, 210); // Darker Blue
    public static final Color BORDER_COLOR = new Color(220, 220, 220);
    public static final Color CARD_COLOR = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(44, 62, 80);
    public static final Color TEXT_SECONDARY = new Color(127, 140, 141);
    public static final Color ACCENT_COLOR = new Color(255, 235, 59); // Yellow
    public static final Color WARNING_COLOR = new Color(241, 196, 15);
    public static final Color DANGER_COLOR = new Color(231, 76, 60);
    
    // Dashboard Colors
    public static final Color DASHBOARD_SIDEBAR = new Color(26, 26, 26); // #1a1a1a
    public static final Color DASHBOARD_BG = new Color(242, 242, 242); // #f2f2f2
    public static final Color DASHBOARD_CARD_DARK = new Color(26, 26, 26);
    public static final Color DASHBOARD_TEXT_LIGHT = new Color(255, 255, 255);
    public static final Color DASHBOARD_SUCCESS = new Color(46, 204, 113); // Green
    public static final Color DASHBOARD_DANGER = new Color(231, 76, 60); // Red
    
    // Fonts
    public static final Font FONT_EMOJI = new Font("Segoe UI Emoji", Font.PLAIN, 48);
    public static final Font FONT_EMOJI_SMALL = new Font("Segoe UI Emoji", Font.PLAIN, 36);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_DIALOG_HEADER = new Font("Segoe UI", Font.BOLD, 18);
    
    // Table Columns
    public static final String[] JOB_COLUMNS = {"ID", "Title", "Company", "Location", "Salary", "Description"};
}
