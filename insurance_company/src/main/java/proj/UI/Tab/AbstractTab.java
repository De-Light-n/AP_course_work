package proj.UI.Tab;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Абстрактний клас вкладки для головного інтерфейсу застосунку.
 * Містить загальні стилі, кольори, шрифти, а також методи для створення
 * стилізованих компонентів.
 */
public abstract class AbstractTab extends JPanel {
    protected static final Logger logger = LogManager.getLogger(AbstractTab.class);
    public static final Color BACKGROUND_COLOR = new Color(240, 240, 240);
    public static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 16);
    public static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final int DEFAULT_PADDING = 10;

    protected final JTabbedPane mainTabbedPane;

    /**
     * Створює вкладку з посиланням на головний JTabbedPane.
     *
     * @param mainTabbedPane головна панель вкладок
     */
    public AbstractTab(JTabbedPane mainTabbedPane) {
        this.mainTabbedPane = mainTabbedPane;
        setLayout(new BorderLayout(DEFAULT_PADDING, DEFAULT_PADDING));
        setBackground(BACKGROUND_COLOR);
    }

    /**
     * Ініціалізує інтерфейс вкладки.
     */
    protected abstract void initializeUI();

    /**
     * Створює стилізовану кнопку для вкладки.
     *
     * @param text текст кнопки
     * @return стилізована кнопка
     */
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

    /**
     * Відображає діалог помилки з заданим повідомленням.
     *
     * @param message текст повідомлення
     */
    protected void showErrorDialog(String message) {
        JOptionPane pane = new JOptionPane(
                message,
                JOptionPane.ERROR_MESSAGE);
        JDialog dialog = pane.createDialog(this, "Помилка");
        dialog.setName("errorDialog");
        dialog.setVisible(true);
    }

    /**
     * Відображає діалог успіху з заданим повідомленням.
     *
     * @param message текст повідомлення
     */
    protected void showSuccessDialog(String message) {
        JOptionPane pane = new JOptionPane(
                message,
                JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = pane.createDialog(this, "Успіх");
        dialog.setName("successDialog");
        dialog.setVisible(true);
    }

    /**
     * Створює стилізоване текстове поле.
     *
     * @param columns кількість колонок
     * @return стилізоване текстове поле
     */
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

    /**
     * Клас для круглого бордера компонентів.
     */
    static class RoundedBorder implements javax.swing.border.Border {
        private final int radius;
        private final Color focusColor;

        /**
         * Створює бордер із заданим радіусом та стандартним кольором.
         *
         * @param radius радіус округлення
         */
        public RoundedBorder(int radius) {
            this(radius, new Color(180, 180, 180));
        }

        /**
         * Створює бордер із заданим радіусом та кольором.
         *
         * @param radius     радіус округлення
         * @param focusColor колір бордера
         */
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