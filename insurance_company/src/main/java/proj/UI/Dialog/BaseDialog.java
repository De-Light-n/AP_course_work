package proj.UI.Dialog;

import javax.swing.*;
import proj.UI.Tab.AbstractTab;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Абстрактний базовий клас для діалогових вікон із кнопками "Зберегти" та
 * "Скасувати".
 * Забезпечує базову структуру, стилі та поведінку для діалогів у застосунку.
 */
public abstract class BaseDialog extends JDialog {
    /** Чи було збережено зміни у діалозі. */
    protected boolean saved = false;
    /** Кнопка збереження. */
    protected JButton saveButton, cancelButton;

    /**
     * Створює базовий діалог із заданим батьківським вікном і заголовком.
     *
     * @param parent батьківське вікно
     * @param title  заголовок діалогу
     */
    public BaseDialog(JFrame parent, String title) {
        super(parent, title, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    /**
     * Ініціалізує кнопки "Зберегти" та "Скасувати" та додає їх у панель.
     *
     * @param buttonPanel панель для розміщення кнопок
     */
    protected void initializeButtons(JPanel buttonPanel) {
        saveButton = new JButton("Зберегти");
        cancelButton = new JButton("Скасувати");
        saveButton.setName("saveButton");
        cancelButton.setName("cancelButton");

        saveButton.addActionListener(this::saveAction);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
    }

    /**
     * Абстрактний метод для обробки дії збереження.
     *
     * @param e подія натискання кнопки "Зберегти"
     */
    protected abstract void saveAction(ActionEvent e);

    /**
     * Застосовує стандартні стилі до кнопок діалогу.
     */
    protected void applyCommonButtonStyles() {
        saveButton.setFont(AbstractTab.DEFAULT_FONT);
        cancelButton.setFont(AbstractTab.DEFAULT_FONT);
        saveButton.setBackground(AbstractTab.PRIMARY_COLOR);
        saveButton.setForeground(Color.WHITE);
        cancelButton.setBackground(AbstractTab.PRIMARY_COLOR);
        cancelButton.setForeground(Color.WHITE);
    }

    /**
     * @return true, якщо зміни були збережені
     */
    public boolean isSaved() {
        return saved;
    }
}