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

public class AddEditDerivativeDialog extends JDialog {
    private JTextField nameField;
    private JButton saveButton, cancelButton;
    private boolean saved = false;
    private Derivative derivative;

    public AddEditDerivativeDialog(JFrame parent, Derivative derivativeToEdit) {
        super(parent, derivativeToEdit == null ? "Додати дериватив" : "Редагувати дериватив", true);
        this.derivative = derivativeToEdit;

        setLayout(new BorderLayout());
        setSize(400, 200);
        setLocationRelativeTo(parent);

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("Назва деривативу:");
        nameLabel.setFont(AbstractTab.DEFAULT_FONT);
        formPanel.add(nameLabel);
        
        nameField = new JTextField();
        nameField.setFont(AbstractTab.DEFAULT_FONT);
        formPanel.add(nameField);

        // Add empty labels for layout
        formPanel.add(new JLabel());
        formPanel.add(new JLabel());

        add(formPanel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        saveButton = new JButton("Зберегти");
        cancelButton = new JButton("Скасувати");
        
        // Apply consistent styling to buttons
        saveButton.setFont(AbstractTab.DEFAULT_FONT);
        cancelButton.setFont(AbstractTab.DEFAULT_FONT);
        saveButton.setBackground(AbstractTab.PRIMARY_COLOR);
        saveButton.setForeground(Color.WHITE);
        cancelButton.setBackground(AbstractTab.PRIMARY_COLOR);
        cancelButton.setForeground(Color.WHITE);

        saveButton.addActionListener(_ -> {
            if (validateInput()) {
                saved = true;
                saveDerivative();
                dispose();
            }
        });

        cancelButton.addActionListener(_ -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Populate fields if editing
        if (derivativeToEdit != null) {
            nameField.setText(derivativeToEdit.getName());
        }
    }

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

    private void saveDerivative() {
        if (derivative == null) {
            derivative = new Derivative(nameField.getText().trim());
        } else {
            derivative.setName(nameField.getText().trim());
        }
    }

    public boolean isSaved() {
        return saved;
    }

    public Derivative getDerivative() {
        return derivative;
    }
}