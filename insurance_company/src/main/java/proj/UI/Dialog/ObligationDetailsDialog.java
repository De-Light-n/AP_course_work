package proj.UI.Dialog;

import proj.Models.insurance.*;
import proj.Models.insurance.InsuranceObligation.ObligationStatus;
import proj.Repositories.InsuranceObligationRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Діалог для перегляду та редагування деталей страхового зобов'язання.
 * Дозволяє змінювати основні параметри, переглядати специфічні поля для різних
 * типів зобов'язань,
 * зберігати зміни та відслідковувати їх.
 */
public class ObligationDetailsDialog extends BaseDialog {
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

    /**
     * Створює діалог для перегляду та редагування зобов'язання.
     *
     * @param parent     батьківське вікно
     * @param obligation страхове зобов'язання
     */
    public ObligationDetailsDialog(JFrame parent, InsuranceObligation obligation) {
        super(parent, "Деталі зобов'язання #" + obligation.getId());
        this.obligation = obligation;
        logger.info("Відкрито діалог деталей зобов'язання з ID: {}", obligation.getId());
        setName("obligationDetailsDialog");
        initializeUI();
        pack();
        setLocationRelativeTo(parent);
    }

    /**
     * Ініціалізує інтерфейс користувача діалогу.
     */
    private void initializeUI() {
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel commonFieldsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        commonFieldsPanel.setBorder(BorderFactory.createTitledBorder("Основні параметри"));

        statusCombo = new JComboBox<>(ObligationStatus.values());
        statusCombo.setSelectedItem(obligation.getStatus());
        statusCombo.setName("statusCombo");
        statusCombo.addActionListener(e -> {
            dataChanged = true;
            logger.debug("Змінено статус зобов'язання на {}", statusCombo.getSelectedItem());
        });

        policyNumberField = new JTextField(obligation.getPolicyNumber());
        policyNumberField.setName("policyNumberField");
        policyNumberField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            dataChanged = true;
            logger.debug("Змінено номер поліса");
        }));

        amountField = new JTextField(String.valueOf(obligation.getAmount()));
        amountField.setName("amountField");
        amountField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            dataChanged = true;
            logger.debug("Змінено суму страхування");
        }));

        riskLevelField = new JTextField(String.valueOf(obligation.getRiskLevel()));
        riskLevelField.setName("riskLevelField");
        riskLevelField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            dataChanged = true;
            logger.debug("Змінено рівень ризику");
        }));

        durationField = new JTextField(String.valueOf(obligation.getDurationMonths()));
        durationField.setName("durationField");
        durationField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            dataChanged = true;
            logger.debug("Змінено тривалість");
        }));

        notesField = new JTextField(obligation.getNotes());
        notesField.setName("notesField");
        notesField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            dataChanged = true;
            logger.debug("Змінено примітки");
        }));

        commonFieldsPanel.add(new JLabel("Статус:"));
        commonFieldsPanel.add(statusCombo);

        JLabel policyNumberLabel = new JLabel("Номер поліса:");
        policyNumberLabel.setName("policyNumberLabel");
        commonFieldsPanel.add(policyNumberLabel);
        commonFieldsPanel.add(policyNumberField);

        JLabel amountLabel = new JLabel("Сума страхування:");
        amountLabel.setName("amountLabel");
        commonFieldsPanel.add(amountLabel);
        commonFieldsPanel.add(amountField);

        JLabel riskLevelLabel = new JLabel("Рівень ризику:");
        riskLevelLabel.setName("riskLevelLabel");
        commonFieldsPanel.add(riskLevelLabel);
        commonFieldsPanel.add(riskLevelField);

        JLabel durationLabel = new JLabel("Тривалість (місяці):");
        durationLabel.setName("durationLabel");
        commonFieldsPanel.add(durationLabel);
        commonFieldsPanel.add(durationField);

        JLabel notesLabel = new JLabel("Примітки:");
        notesLabel.setName("notesLabel");
        commonFieldsPanel.add(notesLabel);
        commonFieldsPanel.add(notesField);

        JLabel calculatedValueLabel = new JLabel("Розрахована вартість:");
        calculatedValueLabel.setName("calculatedValueLabel");
        commonFieldsPanel.add(calculatedValueLabel);

        JLabel calculatedValueValueLabel = new JLabel(String.valueOf(obligation.getCalculatedValue()));
        calculatedValueValueLabel.setName("calculatedValueValueLabel");
        commonFieldsPanel.add(calculatedValueValueLabel);

        mainPanel.add(commonFieldsPanel);

        JPanel typeSpecificPanel = createTypeSpecificPanel();
        mainPanel.add(typeSpecificPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        initializeButtons(buttonPanel);
        applyCommonButtonStyles();

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Повертає репозиторій для роботи із зобов'язаннями.
     *
     * @return репозиторій зобов'язань
     */
    protected InsuranceObligationRepository getRepository() {
        return this.repository;
    }

    /**
     * Створює панель зі специфічними параметрами для типу зобов'язання.
     *
     * @return панель специфічних параметрів
     */
    private JPanel createTypeSpecificPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Специфічні параметри"));
        panel.setLayout(new GridLayout(0, 2, 5, 5));

        if (obligation instanceof HealthInsurance) {
            HealthInsurance health = (HealthInsurance) obligation;

            JLabel ageLabel = new JLabel("Вік:");
            ageLabel.setName("ageLabel");
            panel.add(ageLabel);
            JLabel ageValueLabel = new JLabel(String.valueOf(health.getAge()));
            ageValueLabel.setName("ageValueLabel");
            panel.add(ageValueLabel);

            JLabel preexistingLabel = new JLabel("Хронічні хвороби:");
            preexistingLabel.setName("preexistingLabel");
            panel.add(preexistingLabel);
            JLabel preexistingValueLabel = new JLabel(health.hasPreexistingConditions() ? "Так" : "Ні");
            preexistingValueLabel.setName("preexistingValueLabel");
            panel.add(preexistingValueLabel);

            JLabel coverageLimitLabel = new JLabel("Ліміт покриття:");
            coverageLimitLabel.setName("coverageLimitLabel");
            panel.add(coverageLimitLabel);
            JLabel coverageLimitValueLabel = new JLabel(String.valueOf(health.getCoverageLimit()));
            coverageLimitValueLabel.setName("coverageLimitValueLabel");
            panel.add(coverageLimitValueLabel);

            JLabel hospitalizationLabel = new JLabel("Госпіталізація:");
            hospitalizationLabel.setName("hospitalizationLabel");
            panel.add(hospitalizationLabel);
            JLabel hospitalizationValueLabel = new JLabel(health.includesHospitalization() ? "Так" : "Ні");
            hospitalizationValueLabel.setName("hospitalizationValueLabel");
            panel.add(hospitalizationValueLabel);

            JLabel dentalCareLabel = new JLabel("Стоматологія:");
            dentalCareLabel.setName("dentalCareLabel");
            panel.add(dentalCareLabel);
            JLabel dentalCareValueLabel = new JLabel(health.includesDentalCare() ? "Так" : "Ні");
            dentalCareValueLabel.setName("dentalCareValueLabel");
            panel.add(dentalCareValueLabel);
        } else if (obligation instanceof LifeInsurance) {
            LifeInsurance life = (LifeInsurance) obligation;

            JLabel beneficiaryLabel = new JLabel("Бенефіціар:");
            beneficiaryLabel.setName("beneficiaryLabel");
            panel.add(beneficiaryLabel);
            JLabel beneficiaryValueLabel = new JLabel(life.getBeneficiary());
            beneficiaryValueLabel.setName("beneficiaryValueLabel");
            panel.add(beneficiaryValueLabel);

            JLabel criticalIllnessLabel = new JLabel("Критичні хвороби:");
            criticalIllnessLabel.setName("criticalIllnessLabel");
            panel.add(criticalIllnessLabel);
            JLabel criticalIllnessValueLabel = new JLabel(life.includesCriticalIllness() ? "Так" : "Ні");
            criticalIllnessValueLabel.setName("criticalIllnessValueLabel");
            panel.add(criticalIllnessValueLabel);

            JLabel accidentalDeathLabel = new JLabel("Нещасні випадки:");
            accidentalDeathLabel.setName("accidentalDeathLabel");
            panel.add(accidentalDeathLabel);
            JLabel accidentalDeathValueLabel = new JLabel(life.includesAccidentalDeath() ? "Так" : "Ні");
            accidentalDeathValueLabel.setName("accidentalDeathValueLabel");
            panel.add(accidentalDeathValueLabel);
        } else if (obligation instanceof PropertyInsurance) {
            PropertyInsurance property = (PropertyInsurance) obligation;

            JLabel propertyLocationLabel = new JLabel("Місцезнаходження:");
            propertyLocationLabel.setName("propertyLocationLabel");
            panel.add(propertyLocationLabel);
            JLabel propertyLocationValueLabel = new JLabel(property.getPropertyLocation());
            propertyLocationValueLabel.setName("propertyLocationValueLabel");
            panel.add(propertyLocationValueLabel);

            JLabel propertyValueLabel = new JLabel("Вартість:");
            propertyValueLabel.setName("propertyValueLabel");
            panel.add(propertyValueLabel);
            JLabel propertyValueValueLabel = new JLabel(String.valueOf(property.getPropertyValue()));
            propertyValueValueLabel.setName("propertyValueValueLabel");
            panel.add(propertyValueValueLabel);

            JLabel highRiskAreaLabel = new JLabel("Зона ризику:");
            highRiskAreaLabel.setName("highRiskAreaLabel");
            panel.add(highRiskAreaLabel);
            JLabel highRiskAreaValueLabel = new JLabel(property.isHighRiskArea() ? "Так" : "Ні");
            highRiskAreaValueLabel.setName("highRiskAreaValueLabel");
            panel.add(highRiskAreaValueLabel);

            JLabel propertyTypeLabel = new JLabel("Тип нерухомості:");
            propertyTypeLabel.setName("propertyTypeLabel");
            panel.add(propertyTypeLabel);
            JLabel propertyTypeValueLabel = new JLabel(property.getPropertyType());
            propertyTypeValueLabel.setName("propertyTypeValueLabel");
            panel.add(propertyTypeValueLabel);

            JLabel naturalDisastersLabel = new JLabel("Стихійні лиха:");
            naturalDisastersLabel.setName("naturalDisastersLabel");
            panel.add(naturalDisastersLabel);
            JLabel naturalDisastersValueLabel = new JLabel(property.includesNaturalDisasters() ? "Так" : "Ні");
            naturalDisastersValueLabel.setName("naturalDisastersValueLabel");
            panel.add(naturalDisastersValueLabel);
        }

        return panel;
    }

    /**
     * Обробляє дію збереження змін у зобов'язанні.
     *
     * @param e подія натискання кнопки "Зберегти"
     */
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
        } catch (Exception ex) {
            logger.error("Помилка : {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this,
                    "Будь ласка, перевірте правильність введених даних.\n" +
                            "Помилка: " + ex.getMessage(),
                    "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @return true, якщо дані були змінені
     */
    public boolean isDataChanged() {
        return dataChanged;
    }
}