package view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * UITheme.java
 * -------------
 * Central place for all colors, fonts, and reusable UI components.
 * Every screen uses this so the whole app looks consistent.
 * To change the look of the entire app, only edit this file.
 */
public class UITheme {

    // ---------------------------------------------------------------
    // COLOR PALETTE — Clean & Professional White/Blue
    // ---------------------------------------------------------------
    public static final Color PRIMARY        = new Color(25,  118, 210);  // Main blue
    public static final Color PRIMARY_DARK   = new Color(13,  71,  161);  // Darker blue (hover)
    public static final Color PRIMARY_LIGHT  = new Color(227, 242, 253);  // Light blue background
    public static final Color ACCENT         = new Color(0,   150, 136);  // Teal accent
    public static final Color SUCCESS        = new Color(46,  125, 50);   // Green
    public static final Color WARNING        = new Color(245, 124, 0);    // Orange
    public static final Color DANGER         = new Color(198, 40,  40);   // Red
    public static final Color WHITE          = Color.WHITE;
    public static final Color BG_MAIN        = new Color(245, 247, 250);  // Page background
    public static final Color BG_CARD        = Color.WHITE;               // Card background
    public static final Color BG_SIDEBAR     = new Color(21,  50,  90);   // Dark sidebar
    public static final Color SIDEBAR_TEXT   = new Color(189, 210, 235);  // Sidebar text
    public static final Color SIDEBAR_ACTIVE = new Color(25,  118, 210);  // Active menu item
    public static final Color TEXT_PRIMARY   = new Color(30,  30,  30);   // Main text
    public static final Color TEXT_SECONDARY = new Color(100, 100, 100);  // Subtle text
    public static final Color BORDER_COLOR   = new Color(220, 224, 230);  // Input borders
    public static final Color TABLE_HEADER   = new Color(232, 240, 254);  // Table header bg
    public static final Color TABLE_ALT_ROW  = new Color(249, 251, 255);  // Alternating row

    // ---------------------------------------------------------------
    // FONTS
    // ---------------------------------------------------------------
    public static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADING    = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_SUBHEADING = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BODY       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON     = new Font("Segoe UI Symbol", Font.BOLD,  13);
    public static final Font FONT_INPUT      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_TABLE      = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_TABLE_HDR  = new Font("Segoe UI", Font.BOLD,  12);
    public static final Font FONT_SIDEBAR    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SIDEBAR_HD = new Font("Segoe UI", Font.BOLD,  11);
    public static final Font FONT_EMOJI      = new Font("Segoe UI Emoji", Font.PLAIN, 13);
    // ---------------------------------------------------------------
    // DIMENSIONS
    // ---------------------------------------------------------------
    public static final int SIDEBAR_WIDTH    = 220;
    public static final int TOPBAR_HEIGHT    = 60;
    public static final int BUTTON_HEIGHT    = 36;
    public static final int INPUT_HEIGHT     = 36;
    public static final int CARD_ARC         = 12;

    // ---------------------------------------------------------------
    // BORDERS
    // ---------------------------------------------------------------
    public static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(4, 10, 4, 10)
        );
    }

    public static Border inputBorderFocused() {
        return BorderFactory.createCompoundBorder(
            new LineBorder(PRIMARY, 2, true),
            new EmptyBorder(3, 9, 3, 9)
        );
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        );
    }

    public static Border paddingBorder(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    // ---------------------------------------------------------------
    // REUSABLE COMPONENTS
    // ---------------------------------------------------------------

    /** Standard primary blue button */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_EMOJI);
        btn.setForeground(WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, BUTTON_HEIGHT));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(PRIMARY_DARK); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(PRIMARY); }
        });
        return btn;
    }

    /** Danger/red button for delete actions */
    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_EMOJI);
        btn.setForeground(WHITE);
        btn.setBackground(DANGER);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, BUTTON_HEIGHT));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(183, 28, 28)); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(DANGER); }
        });
        return btn;
    }

    /** Success/green button */
    public static JButton successButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_EMOJI);
        btn.setForeground(WHITE);
        btn.setBackground(SUCCESS);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, BUTTON_HEIGHT));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(27, 94, 32)); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(SUCCESS); }
        });
        return btn;
    }

    /** Outlined/secondary button */
    public static JButton outlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_EMOJI);
        btn.setForeground(PRIMARY);
        btn.setBackground(WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(PRIMARY, 1, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, BUTTON_HEIGHT));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(PRIMARY_LIGHT); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(WHITE); }
        });
        return btn;
    }

    /** Standard text field with styled border */
    public static JTextField textField() {
        JTextField field = new JTextField();
        field.setFont(FONT_INPUT);
        field.setBorder(inputBorder());
        field.setPreferredSize(new Dimension(200, INPUT_HEIGHT));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { field.setBorder(inputBorderFocused()); }
            public void focusLost(java.awt.event.FocusEvent e)   { field.setBorder(inputBorder()); }
        });
        return field;
    }

    /** Password field with styled border */
    public static JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(FONT_INPUT);
        field.setBorder(inputBorder());
        field.setPreferredSize(new Dimension(200, INPUT_HEIGHT));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { field.setBorder(inputBorderFocused()); }
            public void focusLost(java.awt.event.FocusEvent e)   { field.setBorder(inputBorder()); }
        });
        return field;
    }

    /** Combo box with styled border */
    public static JComboBox<String> comboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(FONT_INPUT);
        combo.setBackground(WHITE);
        combo.setPreferredSize(new Dimension(200, INPUT_HEIGHT));
        return combo;
    }

    /** Section label (bold heading inside a form) */
    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SUBHEADING);
        lbl.setForeground(PRIMARY);
        return lbl;
    }

    /** Form field label */
    public static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    /** Styled JTable with alternating rows and custom header */
    public static void styleTable(JTable table) {
        table.setFont(FONT_TABLE);
        table.setRowHeight(32);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setFont(FONT_TABLE_HDR);
        table.getTableHeader().setBackground(TABLE_HEADER);
        table.getTableHeader().setForeground(PRIMARY_DARK);
        table.getTableHeader().setBorder(new LineBorder(BORDER_COLOR, 1));
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? WHITE : TABLE_ALT_ROW);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    /** Apply consistent look and feel to the whole app */
    public static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Button.arc",           10);
            UIManager.put("Component.arc",        8);
            UIManager.put("Panel.background",     BG_MAIN);
            UIManager.put("OptionPane.background", WHITE);
            UIManager.put("OptionPane.messageFont", FONT_BODY);
        } catch (Exception e) {
            // Fall back to default look and feel silently
        }
    }
}
