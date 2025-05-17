package proj.UI.Dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import proj.Models.Derivative;
import proj.UI.Tab.AbstractTab;

/**
 * Діалог для додавання або редагування деривативу.
 * Дозволяє ввести або змінити назву деривативу.
 */
public class AddEditDerivativeDialog extends JDialog {
    private JTextField nameField;
    private JButton saveButton, cancelButton;
    private boolean saved = false;
    private Derivative derivative;

    /**
     * Створює діалог для додавання або редагування деривативу.
     *
     * @param parent           батьківське вікно
     * @param derivativeToEdit дериватив для редагування або null для створення
     *                         нового
     */
    public AddEditDerivativeDialog(JFrame parent, Derivative derivativeToEdit) {
        super(parent, derivativeToEdit == null ? "Додати дериватив" : "Редагувати дериватив", true);
        this.derivative = derivativeToEdit;

        setLayout(new BorderLayout());
        setSize(400, 200);
        setLocationRelativeTo(parent);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("Назва деривативу:");
        nameLabel.setFont(AbstractTab.DEFAULT_FONT);
        formPanel.add(nameLabel);

        nameField = new JTextField();
        nameField.setFont(AbstractTab.DEFAULT_FONT);
        formPanel.add(nameField);

        formPanel.add(new JLabel());
        formPanel.add(new JLabel());

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        saveButton = new JButton("Зберегти");
        cancelButton = new JButton("Скасувати");
        saveButton.setName("saveButton");
        cancelButton.setName("cancelButton");

        saveButton.setFont(AbstractTab.DEFAULT_FONT);
        cancelButton.setFont(AbstractTab.DEFAULT_FONT);
        saveButton.setBackground(AbstractTab.PRIMARY_COLOR);
        saveButton.setForeground(Color.WHITE);
        cancelButton.setBackground(AbstractTab.PRIMARY_COLOR);
        cancelButton.setForeground(Color.WHITE);

        saveButton.addActionListener(e -> {
            if (validateInput()) {
                saved = true;
                saveDerivative();
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        if (derivativeToEdit != null) {
            nameField.setText(derivativeToEdit.getName());
        }
    }

    /**
     * Перевіряє коректність введених даних.
     *
     * @return true, якщо дані коректні
     */
    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Назва деривативу не може бути порожньою",
                    "Помилка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Зберігає або оновлює дериватив на основі введених даних.
     */
    private void saveDerivative() {
        if (derivative == null) {
            derivative = new Derivative(nameField.getText().trim());
        } else {
            derivative.setName(nameField.getText().trim());
        }
    }

    /**
     * @return true, якщо дериватив було збережено
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * @return створений або відредагований дериватив
     */
    public Derivative getDerivative() {
        return derivative;
    }
}