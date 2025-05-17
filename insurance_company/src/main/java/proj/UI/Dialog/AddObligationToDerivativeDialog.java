package proj.UI.Dialog;

import proj.Models.Derivative;
import proj.Models.insurance.*;
import proj.Repositories.InsuranceObligationRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;

/**
 * Діалог для додавання нового страхового зобов'язання до деривативу.
 * Дозволяє вибрати тип зобов'язання, ввести основні та специфічні параметри,
 * та зберегти зобов'язання у відповідний дериватив.
 */
public class AddObligationToDerivativeDialog extends BaseDialog {
    private static final Logger logger = LogManager.getLogger(AddObligationToDerivativeDialog.class);

    private InsuranceObligation obligation;
    private final Derivative derivative;
    private final InsuranceObligationRepository repository = new InsuranceObligationRepository();

    private JComboBox<String> obligationType;
    private JPanel detailsPanel;
    private JTextField riskLevelField, amountField, durationField;
    private JTextField policyNumberField, notesField;

    /**
     * Створює діалог для додавання зобов'язання до деривативу.
     *
     * @param parent     батьківське вікно
     * @param derivative дериватив, до якого додається зобов'язання
     */
    public AddObligationToDerivativeDialog(JFrame parent, Derivative derivative) {
        super(parent, "Додати зобов'язання до деривативу");
        setName("addObligationDialog");
        this.derivative = derivative;
        logger.info("Ініціалізація діалогу для додавання зобов'язання до деривативу: {}", derivative.getName());
        initializeUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        logger.debug("Ініціалізація інтерфейсу користувача для діалогу");
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(new JLabel("Тип зобов'язання:"));
        obligationType = new JComboBox<>(new String[] { "HealthInsurance", "LifeInsurance", "PropertyInsurance" });
        obligationType.addActionListener(this::updateDetailsPanel);
        typePanel.add(obligationType);

        JPanel commonFieldsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        commonFieldsPanel.setBorder(BorderFactory.createTitledBorder("Основні параметри"));

        commonFieldsPanel.add(new JLabel("Рівень ризику (0-1):"));
        riskLevelField = new JTextField();
        riskLevelField.setName("riskLevelField");
        commonFieldsPanel.add(riskLevelField);

        commonFieldsPanel.add(new JLabel("Сума страхування:"));
        amountField = new JTextField();
        amountField.setName("amountField");
        commonFieldsPanel.add(amountField);

        commonFieldsPanel.add(new JLabel("Тривалість (місяці):"));
        durationField = new JTextField();
        durationField.setName("durationField");
        commonFieldsPanel.add(durationField);

        commonFieldsPanel.add(new JLabel("Номер поліса:"));
        policyNumberField = new JTextField();
        policyNumberField.setName("policyNumberField");
        commonFieldsPanel.add(policyNumberField);

        commonFieldsPanel.add(new JLabel("Примітки:"));
        notesField = new JTextField();
        notesField.setName("notesField");
        commonFieldsPanel.add(notesField);

        detailsPanel = new JPanel();
        detailsPanel.setName("detailsPanel");
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Специфічні параметри"));
        updateDetailsPanel(null);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        initializeButtons(buttonPanel);
        applyCommonButtonStyles();

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(typePanel);
        mainPanel.add(commonFieldsPanel);
        mainPanel.add(detailsPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Оновлює панель специфічних параметрів відповідно до вибраного типу
     * зобов'язання.
     *
     * @param e подія вибору типу
     */
    private void updateDetailsPanel(ActionEvent e) {
        detailsPanel.removeAll();
        String selectedType = (String) obligationType.getSelectedItem();
        logger.debug("Оновлення панелі деталей для типу зобов'язання: {}", selectedType);

        switch (selectedType) {
            case "HealthInsurance":
                createHealthInsurancePanel();
                break;
            case "LifeInsurance":
                createLifeInsurancePanel();
                break;
            case "PropertyInsurance":
                createPropertyInsurancePanel();
                break;
        }

        detailsPanel.revalidate();
        detailsPanel.repaint();
        pack();
    }

    private void createHealthInsurancePanel() {
        detailsPanel.setLayout(new GridLayout(0, 2, 5, 5));

        JTextField ageField = new JTextField();
        ageField.setName("ageField");

        JCheckBox preexistingConditionsCheck = new JCheckBox();
        preexistingConditionsCheck.setName("preexistingConditionsCheck");

        JTextField coverageLimitField = new JTextField();
        coverageLimitField.setName("coverageLimitField");

        JCheckBox hospitalizationCheck = new JCheckBox();
        hospitalizationCheck.setName("hospitalizationCheck");

        JCheckBox dentalCareCheck = new JCheckBox();
        dentalCareCheck.setName("dentalCareCheck");

        detailsPanel.add(new JLabel("Вік:"));
        detailsPanel.add(ageField);
        detailsPanel.add(new JLabel("Наявність хронічних хвороб:"));
        detailsPanel.add(preexistingConditionsCheck);
        detailsPanel.add(new JLabel("Ліміт покриття:"));
        detailsPanel.add(coverageLimitField);
        detailsPanel.add(new JLabel("Включає госпіталізацію:"));
        detailsPanel.add(hospitalizationCheck);
        detailsPanel.add(new JLabel("Включає стоматологію:"));
        detailsPanel.add(dentalCareCheck);
    }

    private void createLifeInsurancePanel() {
        detailsPanel.setLayout(new GridLayout(0, 2, 5, 5));

        JTextField beneficiaryField = new JTextField();
        beneficiaryField.setName("beneficiaryField");

        JCheckBox criticalIllnessCheck = new JCheckBox();
        criticalIllnessCheck.setName("criticalIllnessCheck");

        JCheckBox accidentalDeathCheck = new JCheckBox();
        accidentalDeathCheck.setName("accidentalDeathCheck");

        detailsPanel.add(new JLabel("Бенефіціар:"));
        detailsPanel.add(beneficiaryField);
        detailsPanel.add(new JLabel("Включає критичні хвороби:"));
        detailsPanel.add(criticalIllnessCheck);
        detailsPanel.add(new JLabel("Включає смерть від нещасного випадку:"));
        detailsPanel.add(accidentalDeathCheck);
    }

    private void createPropertyInsurancePanel() {
        detailsPanel.setLayout(new GridLayout(0, 2, 5, 5));

        JTextField locationField = new JTextField();
        locationField.setName("locationField");

        JTextField propertyValueField = new JTextField();
        propertyValueField.setName("propertyValueField");

        JCheckBox highRiskAreaCheck = new JCheckBox();
        highRiskAreaCheck.setName("highRiskAreaCheck");

        JComboBox<String> propertyTypeCombo = new JComboBox<>(
                PropertyInsurance.VALID_PROPERTY_TYPES.toArray(new String[0]));
        propertyTypeCombo.setName("propertyTypeCombo");

        JCheckBox naturalDisastersCheck = new JCheckBox();
        naturalDisastersCheck.setName("naturalDisastersCheck");

        detailsPanel.add(new JLabel("Місцезнаходження:"));
        detailsPanel.add(locationField);
        detailsPanel.add(new JLabel("Вартість нерухомості:"));
        detailsPanel.add(propertyValueField);
        detailsPanel.add(new JLabel("Зона підвищеного ризику:"));
        detailsPanel.add(highRiskAreaCheck);
        detailsPanel.add(new JLabel("Тип нерухомості:"));
        detailsPanel.add(propertyTypeCombo);
        detailsPanel.add(new JLabel("Включає стихійні лиха:"));
        detailsPanel.add(naturalDisastersCheck);
    }

    /**
     * Обробляє дію збереження зобов'язання.
     *
     * @param e подія натискання кнопки збереження
     */
    @Override
    protected void saveAction(ActionEvent e) {
        try {
            logger.info("Збереження зобов'язання для деривативу: {}", derivative.getName());
            String selectedType = (String) obligationType.getSelectedItem();

            double riskLevel = Double.parseDouble(riskLevelField.getText());
            double amount = Double.parseDouble(amountField.getText());
            int duration = Integer.parseInt(durationField.getText());
            String policyNumber = policyNumberField.getText().isEmpty() ? null : policyNumberField.getText();
            String notes = notesField.getText().isEmpty() ? null : notesField.getText();

            switch (selectedType) {
                case "HealthInsurance":
                    obligation = createHealthInsurance(riskLevel, amount, duration);
                    break;
                case "LifeInsurance":
                    obligation = createLifeInsurance(riskLevel, amount, duration);
                    break;
                case "PropertyInsurance":
                    obligation = createPropertyInsurance(riskLevel, amount, duration);
                    break;
            }

            if (policyNumber != null)
                obligation.setPolicyNumber(policyNumber);
            if (notes != null)
                obligation.setNotes(notes);
            obligation.setStatus(InsuranceObligation.ObligationStatus.ACTIVE);
            obligation.setStartDate(LocalDateTime.now());
            obligation.setEndDate(LocalDateTime.now().plusMonths(duration));

            repository.save(obligation, derivative);
            logger.info("Зобов'язання успішно збережено та додано до деривативи");

            saved = true;
            dispose();
        } catch (Exception ex) {
            logger.error("Помилка : {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this,
                    "Будь ласка, перевірте правильність введених даних.",
                    "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private HealthInsurance createHealthInsurance(double riskLevel, double amount, int duration) {
        Component[] components = detailsPanel.getComponents();

        int age = Integer.parseInt(((JTextField) components[1]).getText());
        boolean hasConditions = ((JCheckBox) components[3]).isSelected();
        int coverageLimit = Integer.parseInt(((JTextField) components[5]).getText());
        boolean hospitalization = ((JCheckBox) components[7]).isSelected();
        boolean dentalCare = ((JCheckBox) components[9]).isSelected();

        return new HealthInsurance(riskLevel, amount, duration,
                age, hasConditions,
                coverageLimit, hospitalization,
                dentalCare);
    }

    private LifeInsurance createLifeInsurance(double riskLevel, double amount, int duration) {
        Component[] components = detailsPanel.getComponents();

        String beneficiary = ((JTextField) components[1]).getText();
        boolean criticalIllness = ((JCheckBox) components[3]).isSelected();
        boolean accidentalDeath = ((JCheckBox) components[5]).isSelected();

        return new LifeInsurance(riskLevel, amount, duration,
                beneficiary, criticalIllness,
                accidentalDeath);
    }

    private PropertyInsurance createPropertyInsurance(double riskLevel, double amount, int duration) {
        Component[] components = detailsPanel.getComponents();

        String location = ((JTextField) components[1]).getText();
        double propertyValue = Double.parseDouble(((JTextField) components[3]).getText());
        boolean highRiskArea = ((JCheckBox) components[5]).isSelected();
        String propertyType = (String) ((JComboBox<?>) components[7]).getSelectedItem();
        boolean naturalDisasters = ((JCheckBox) components[9]).isSelected();

        return new PropertyInsurance(riskLevel, amount, duration,
                location, propertyValue,
                highRiskArea, propertyType,
                naturalDisasters);
    }

    /**
     * Повертає створене зобов'язання.
     *
     * @return страхове зобов'язання
     */
    public InsuranceObligation getObligation() {
        return obligation;
    }
}