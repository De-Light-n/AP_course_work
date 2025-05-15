package proj.UI.Dialog;

import javax.swing.*;

import proj.UI.Tab.AbstractTab;

import java.awt.*;
import java.awt.event.ActionEvent;

public abstract class BaseDerivativeDialog extends JDialog {
    protected boolean saved = false;
    protected JButton saveButton, cancelButton;

    public BaseDerivativeDialog(JFrame parent, String title) {
        super(parent, title, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    protected void initializeButtons(JPanel buttonPanel) {
        saveButton = new JButton("Зберегти");
        cancelButton = new JButton("Скасувати");

        saveButton.addActionListener(this::saveAction);
        cancelButton.addActionListener(_ -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
    }

    protected abstract void saveAction(ActionEvent e);

    protected void applyCommonButtonStyles() {
        saveButton.setFont(AbstractTab.DEFAULT_FONT);
        cancelButton.setFont(AbstractTab.DEFAULT_FONT);
        saveButton.setBackground(AbstractTab.PRIMARY_COLOR);
        saveButton.setForeground(Color.WHITE);
        cancelButton.setBackground(AbstractTab.PRIMARY_COLOR);
        cancelButton.setForeground(Color.WHITE);
    }

    public boolean isSaved() {
        return saved;
    }
}