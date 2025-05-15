package proj;

import proj.Database.DatabaseManager;
import proj.UI.Tab.DerivativesTab;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

/**
 * Головний клас програми страхування з інтеграцією деривативів
 */
public class InsuranceApp extends JFrame {

    private static final Logger logger = LogManager.getLogger(InsuranceApp.class);
    private static DatabaseManager dbManager;

    public InsuranceApp() {
        logger.info("Ініціалізація головного вікна InsuranceApp");

        // Ініціалізація бази даних
        try {
            logger.debug("Спроба отримати екземпляр DatabaseManager...");
            dbManager = DatabaseManager.getInstance();
            if (dbManager == null) {
                logger.error("DatabaseManager повернув null. Перевірте конфігурацію бази даних.");
                throw new IllegalStateException("DatabaseManager не ініціалізовано. Перевірте конфігурацію.");
            }
            logger.info("База даних успішно ініціалізована");
        } catch (Exception e) {
            logger.fatal("Невідома помилка під час ініціалізації бази даних: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                    "Помилка підключення до бази даних: " + e.getMessage(),
                    "Помилка бази даних",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("Система страхування");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Додаємо вкладки
        try {
            logger.info("Додавання вкладки 'Деривативи'");
            tabbedPane.addTab("Деривативи", new DerivativesTab(tabbedPane));
        } catch (Exception e) {
            logger.error("Помилка під час додавання вкладки 'Деривативи': {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(this,
                    "Помилка під час ініціалізації вкладки 'Деривативи': " + e.getMessage(),
                    "Помилка вкладки",
                    JOptionPane.ERROR_MESSAGE);
        }

        add(tabbedPane);

        logger.info("Головне вікно InsuranceApp ініціалізовано");
    }

    public static void main(String[] args) {
        logger.info("Запуск програми InsuranceApp");

        SwingUtilities.invokeLater(() -> {
            try {
                logger.debug("Спроба створити екземпляр InsuranceApp...");
                new InsuranceApp().setVisible(true);
            } catch (Exception e) {
                logger.error("Помилка запуску програми: {}", e.getMessage(), e);
                JOptionPane.showMessageDialog(null,
                        "Критична помилка: " + e.getMessage(),
                        "Помилка запуску",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Додаємо shutdown hook для коректного закриття підключення до БД
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (dbManager != null) {
                    logger.debug("Спроба закрити з'єднання з базою даних...");
                    dbManager.close();
                    logger.info("З'єднання з базою даних закрито");
                }
            } catch (Exception e) {
                logger.error("Помилка під час закриття з'єднання з базою даних: {}", e.getMessage(), e);
            }
            logger.info("Програма InsuranceApp завершила роботу");
        }));
    }
}