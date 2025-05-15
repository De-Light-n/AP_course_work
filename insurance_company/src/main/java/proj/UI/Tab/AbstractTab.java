package proj.UI.Tab;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public abstract class AbstractTab extends JPanel {
    protected static final Logger logger = LogManager.getLogger(AbstractTab.class);
    public static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    public static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 16);
    public static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final int DEFAULT_PADDING = 10;

    protected final JTabbedPane mainTabbedPane;

    public AbstractTab(JTabbedPane mainTabbedPane) {
        this.mainTabbedPane = mainTabbedPane;
        setLayout(new BorderLayout(DEFAULT_PADDING, DEFAULT_PADDING));
        setBackground(BACKGROUND_COLOR);
    }

    protected abstract void initializeUI();

    protected ImageIcon loadIconResource(String path) {
        try {
            URL resourceUrl = getClass().getResource(path);
            if (resourceUrl != null) {
                return new ImageIcon(resourceUrl);
            } else {
                logger.warn("Не вдалося знайти ресурс: {}", path);
                return null;
            }
        } catch (Exception e) {
            logger.warn("Помилка завантаження ресурсу: {}", path, e);
            return null;
        }
    }

    protected JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque() && getBorder() instanceof RoundedBorder) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        button.setFont(DEFAULT_FONT);
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setBorder(new RoundedBorder(9));
        button.setMargin(new Insets(4, 8, 4, 8));
        button.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                button.setBorder(new RoundedBorder(9, Color.GRAY));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                button.setBorder(new RoundedBorder(9));
            }
        });
        return button;
    }

    protected JTextField createStyledTextField(int columns) {
        JTextField textField = new JTextField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque() && getBorder() instanceof RoundedBorder) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        textField.setOpaque(false);
        textField.setBorder(new RoundedBorder(9));
        textField.setBackground(Color.WHITE);
        textField.setFont(DEFAULT_FONT);
        textField.setMargin(new Insets(4, 8, 4, 8));
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                textField.setBorder(new RoundedBorder(9, PRIMARY_COLOR));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                textField.setBorder(new RoundedBorder(9));
            }
        });
        return textField;
    }

    protected JTextArea createStyledTextArea(int rows, int columns) {
        JTextArea textArea = new JTextArea(rows, columns) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque() && getBorder() instanceof RoundedBorder) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        textArea.setOpaque(false);
        textArea.setBorder(new RoundedBorder(9));
        textArea.setBackground(Color.WHITE);
        textArea.setFont(DEFAULT_FONT);
        textArea.setMargin(new Insets(4, 8, 4, 8));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                textArea.setBorder(new RoundedBorder(9, PRIMARY_COLOR));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                textArea.setBorder(new RoundedBorder(9));
            }
        });
        return textArea;
    }

    protected void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, 
            message, 
            "Помилка", 
            JOptionPane.ERROR_MESSAGE);
    }

    protected void showSuccessDialog(String message) {
        JOptionPane.showMessageDialog(this, 
            message, 
            "Успіх", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    protected JTextField createFancyTextField(int columns) {
        JTextField field = new JTextField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque() && getBorder() instanceof RoundedBorder) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setBorder(new RoundedBorder(9));
        field.setBackground(Color.WHITE);
        field.setFont(DEFAULT_FONT);
        field.setMargin(new Insets(4, 8, 4, 8));
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(new RoundedBorder(9, PRIMARY_COLOR));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(new RoundedBorder(9));
            }
        });
        return field;
    }

    // Клас для круглого бордера
    static class RoundedBorder implements javax.swing.border.Border {
        private final int radius;
        private final Color focusColor;

        public RoundedBorder(int radius) {
            this(radius, new Color(180, 180, 180));
        }

        public RoundedBorder(int radius, Color focusColor) {
            this.radius = radius;
            this.focusColor = focusColor;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 8, 4, 8);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(focusColor);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, radius * 2, radius * 2);
            g2.dispose();
        }
    }
}