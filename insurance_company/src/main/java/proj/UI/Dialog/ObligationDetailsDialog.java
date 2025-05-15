package proj.UI.Dialog;

import proj.Models.insurance.*;
import proj.Models.insurance.InsuranceObligation.ObligationStatus;
import proj.Repositories.InsuranceObligationRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

public class ObligationDetailsDialog extends BaseDerivativeDialog {
    private static final Logger logger = LogManager.getLogger(ObligationDetailsDialog.class);

    private final InsuranceObligation obligation;
    private final InsuranceObligationRepository repository = new InsuranceObligationRepository();
    private boolean dataChanged = false;

    private JComboBox<ObligationStatus> statusCombo;
    private JTextField policyNumberField;
    private JTextField amountField;
    private JTextField riskLevelField;
    private JTextField durationField;
    private JTextField notesField;

    public ObligationDetailsDialog(JFrame parent, InsuranceObligation obligation) {
        super(parent, "Деталі зобов'язання #" + obligation.getId());
        this.obligation = obligation;
        logger.info("Відкрито діалог деталей зобов'язання з ID: {}", obligation.getId());
        initializeUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Main panel with scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Common fields panel
        JPanel commonFieldsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        commonFieldsPanel.setBorder(BorderFactory.createTitledBorder("Основні параметри"));

        statusCombo = new JComboBox<>(ObligationStatus.values());
        statusCombo.setSelectedItem(obligation.getStatus());
        statusCombo.addActionListener(_ -> {
            dataChanged = true;
            logger.debug("Змінено статус зобов'язання на {}", statusCombo.getSelectedItem());
        });

        policyNumberField = new JTextField(obligation.getPolicyNumber());
        policyNumberField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            dataChanged = true;
            logger.debug("Змінено номер поліса");
        }));

        amountField = new JTextField(String.valueOf(obligation.getAmount()));
        amountField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            dataChanged = true;
            logger.debug("Змінено суму страхування");
        }));

        riskLevelField = new JTextField(String.valueOf(obligation.getRiskLevel()));
        riskLevelField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            dataChanged = true;
            logger.debug("Змінено рівень ризику");
        }));

        durationField = new JTextField(String.valueOf(obligation.getDurationMonths()));
        durationField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            dataChanged = true;
            logger.debug("Змінено тривалість");
        }));

        notesField = new JTextField(obligation.getNotes());
        notesField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            dataChanged = true;
            logger.debug("Змінено примітки");
        }));

        commonFieldsPanel.add(new JLabel("Статус:"));
        commonFieldsPanel.add(statusCombo);
        commonFieldsPanel.add(new JLabel("Номер поліса:"));
        commonFieldsPanel.add(policyNumberField);
        commonFieldsPanel.add(new JLabel("Сума страхування:"));
        commonFieldsPanel.add(amountField);
        commonFieldsPanel.add(new JLabel("Рівень ризику:"));
        commonFieldsPanel.add(riskLevelField);
        commonFieldsPanel.add(new JLabel("Тривалість (місяці):"));
        commonFieldsPanel.add(durationField);
        commonFieldsPanel.add(new JLabel("Примітки:"));
        commonFieldsPanel.add(notesField);

        // Додаємо відображення calculatedValue
        commonFieldsPanel.add(new JLabel("Розрахована вартість:"));
        commonFieldsPanel.add(new JLabel(String.valueOf(obligation.getCalculatedValue())));

        mainPanel.add(commonFieldsPanel);

        // Type-specific panel
        JPanel typeSpecificPanel = createTypeSpecificPanel();
        mainPanel.add(typeSpecificPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        initializeButtons(buttonPanel);
        applyCommonButtonStyles();

        // Add components
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTypeSpecificPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Специфічні параметри"));
        panel.setLayout(new GridLayout(0, 2, 5, 5));

        if (obligation instanceof HealthInsurance) {
            HealthInsurance health = (HealthInsurance) obligation;
            panel.add(new JLabel("Вік:"));
            panel.add(new JLabel(String.valueOf(health.getAge())));
            panel.add(new JLabel("Хронічні хвороби:"));
            panel.add(new JLabel(health.hasPreexistingConditions() ? "Так" : "Ні"));
            panel.add(new JLabel("Ліміт покриття:"));
            panel.add(new JLabel(String.valueOf(health.getCoverageLimit())));
            panel.add(new JLabel("Госпіталізація:"));
            panel.add(new JLabel(health.includesHospitalization() ? "Так" : "Ні"));
            panel.add(new JLabel("Стоматологія:"));
            panel.add(new JLabel(health.includesDentalCare() ? "Так" : "Ні"));
        } else if (obligation instanceof LifeInsurance) {
            LifeInsurance life = (LifeInsurance) obligation;
            panel.add(new JLabel("Бенефіціар:"));
            panel.add(new JLabel(life.getBeneficiary()));
            panel.add(new JLabel("Критичні хвороби:"));
            panel.add(new JLabel(life.includesCriticalIllness() ? "Так" : "Ні"));
            panel.add(new JLabel("Нещасні випадки:"));
            panel.add(new JLabel(life.includesAccidentalDeath() ? "Так" : "Ні"));
        } else if (obligation instanceof PropertyInsurance) {
            PropertyInsurance property = (PropertyInsurance) obligation;
            panel.add(new JLabel("Місцезнаходження:"));
            panel.add(new JLabel(property.getPropertyLocation()));
            panel.add(new JLabel("Вартість:"));
            panel.add(new JLabel(String.valueOf(property.getPropertyValue())));
            panel.add(new JLabel("Зона ризику:"));
            panel.add(new JLabel(property.isHighRiskArea() ? "Так" : "Ні"));
            panel.add(new JLabel("Тип нерухомості:"));
            panel.add(new JLabel(property.getPropertyType()));
            panel.add(new JLabel("Стихійні лиха:"));
            panel.add(new JLabel(property.includesNaturalDisasters() ? "Так" : "Ні"));
        }

        return panel;
    }

    @Override
    protected void saveAction(ActionEvent e) {
        try {
            logger.info("Спроба зберегти зобов'язання з ID: {}", obligation.getId());
            obligation.setStatus((ObligationStatus) statusCombo.getSelectedItem());
            obligation.setPolicyNumber(policyNumberField.getText());
            obligation.setAmount(Double.parseDouble(amountField.getText()));
            obligation.setRiskLevel(Double.parseDouble(riskLevelField.getText()));
            obligation.setDurationMonths(Integer.parseInt(durationField.getText()));
            obligation.setNotes(notesField.getText());

            obligation.calculateValue();
            repository.save(obligation);
            logger.info("Зобов'язання з ID {} успішно збережено", obligation.getId());
            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            logger.error("Помилка парсингу числових значень: {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this,
                    "Будь ласка, введіть коректні числові значення",
                    "Помилка вводу", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            logger.error("Помилка при збереженні зобов'язання: {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this,
                    "Помилка при збереженні зобов'язання: " + ex.getMessage(),
                    "Помилка бази даних", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            logger.error("Помилка валідації зобов'язання: {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Помилка валідації", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDataChanged() {
        return dataChanged;
    }
}