package proj.UI.Tab;

import proj.Models.Derivative;
import proj.Models.insurance.InsuranceObligation;
import proj.Repositories.DerivativeRepository;
import proj.Repositories.InsuranceObligationRepository;
import proj.Service.InsuranceService;
import proj.Service.DerivativeService;

import proj.UI.Dialog.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Вкладка для перегляду та керування деталями деривативи.
 * Дозволяє переглядати, додавати, видаляти страхові зобов'язання, фільтрувати
 * та сортувати їх,
 * а також видаляти саму деривативу.
 */
public class DerivativeDetailsTab extends AbstractTab {
    private static final Logger logger = LogManager.getLogger(DerivativeDetailsTab.class);

    private final InsuranceService insuranceService = InsuranceService.getInstance();
    private final DerivativeService derivativeService = DerivativeService.getInstance();
    private final Derivative derivative;
    private DefaultTableModel obligationsModel;
    private JTable obligationsTable;
    private JButton addButton, deleteButton;
    private JComboBox<String> sortOptions;
    private JTextField searchField;
    private JTextField minCalcValueField, maxCalcValueField;

    /**
     * Створює вкладку деталей для заданої деривативи.
     *
     * @param mainTabbedPane головна панель вкладок
     * @param derivative     дериватива для перегляду та керування
     */
    public DerivativeDetailsTab(JTabbedPane mainTabbedPane, Derivative derivative) {
        super(mainTabbedPane);
        this.derivative = derivative;
        initializeUI();
    }

    /**
     * Ініціалізує інтерфейс вкладки.
     */
    @Override
    protected void initializeUI() {
        logger.info("Ініціалізація вкладки деталей деривативи: {}", derivative.getName());
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftButtons.setOpaque(false);
        addButton = createStyledButton("Додати страхування");
        addButton.setName("addButton");
        addButton.addActionListener(new AddButtonListener());
        leftButtons.add(addButton);

        deleteButton = createStyledButton("Видалити страхування");
        deleteButton.setName("deleteButton");
        deleteButton.addActionListener(new DeleteButtonListener());
        leftButtons.add(deleteButton);

        buttonPanel.add(leftButtons, BorderLayout.WEST);

        JButton removeDerivativeButton = createStyledButton("Видалити директиву");
        removeDerivativeButton.setName("removeDerivativeButton");
        removeDerivativeButton.addActionListener(new RemoveDerivativeListener());
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightButtonPanel.setOpaque(false);
        rightButtonPanel.add(removeDerivativeButton);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);

        JPanel filterSortPanel = new JPanel();
        filterSortPanel.setLayout(new BoxLayout(filterSortPanel, BoxLayout.Y_AXIS));
        filterSortPanel.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Пошук (за номером полісу, типом, ризиком, сумою, статусом):"));
        searchField = createFancyTextField(15);
        searchField.setName("searchField");
        searchPanel.add(searchField);
        filterSortPanel.add(searchPanel);

        JPanel calcValuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        calcValuePanel.setOpaque(false);
        calcValuePanel.add(new JLabel("Розрах. вартість:"));
        minCalcValueField = createFancyTextField(7);
        minCalcValueField.setName("minCalcValueField");
        calcValuePanel.add(minCalcValueField);
        calcValuePanel.add(new JLabel("-"));
        maxCalcValueField = createFancyTextField(7);
        maxCalcValueField.setName("maxCalcValueField");
        calcValuePanel.add(maxCalcValueField);
        filterSortPanel.add(calcValuePanel);

        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        sortPanel.setOpaque(false);
        sortPanel.add(new JLabel("Сортувати:"));
        sortOptions = new JComboBox<>(new String[] {
                "Номер полісу (зростання)",
                "Номер полісу (спадання)",
                "Тип",
                "Рівень ризику (зростання)",
                "Рівень ризику (спадання)",
                "Сума (зростання)",
                "Сума (спадання)",
                "Розрах. вартість (зростання)",
                "Розрах. вартість (спадання)",
                "Статус"
        });
        sortOptions.setName("sortOptions");
        sortOptions.addActionListener(e -> filterAndSortObligations());
        sortPanel.add(sortOptions);
        filterSortPanel.add(sortPanel);

        JPanel searchSortPanel = new JPanel();
        searchSortPanel.setLayout(new BorderLayout());
        searchSortPanel.setOpaque(false);
        searchSortPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 3, true),
                "Фільтри та інструменти",
                TitledBorder.LEFT, TitledBorder.TOP,
                DEFAULT_FONT.deriveFont(Font.BOLD, 13)));
        searchSortPanel.add(buttonPanel, BorderLayout.NORTH);
        searchSortPanel.add(filterSortPanel, BorderLayout.CENTER);

        JPanel summaryPanel = createSummaryPanel();

        createObligationsTable();
        JScrollPane scrollPane = new JScrollPane(obligationsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 3, true),
                "Директиви",
                TitledBorder.LEFT, TitledBorder.TOP,
                DEFAULT_FONT.deriveFont(Font.BOLD, 13)));
        tablePanel.setOpaque(false);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(120, 120, 180), 4, true),
                "Контроль директивою",
                TitledBorder.LEFT, TitledBorder.TOP,
                DEFAULT_FONT.deriveFont(Font.BOLD, 14)));
        mainPanel.setOpaque(false);

        mainPanel.add(searchSortPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        DocumentListener filterListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterAndSortObligations();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterAndSortObligations();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterAndSortObligations();
            }
        };
        minCalcValueField.getDocument().addDocumentListener(filterListener);
        maxCalcValueField.getDocument().addDocumentListener(filterListener);
        searchField.getDocument().addDocumentListener(filterListener);
    }

    /**
     * Створює панель з короткою інформацією про деривативу.
     *
     * @return панель з інформацією
     */
    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3));
        summaryPanel.setBorder(new TitledBorder("Зведена інформація"));
        summaryPanel.setBackground(BACKGROUND_COLOR);

        JLabel totalLabel = new JLabel("Загальна вартість: " + derivative.getTotalValue());
        totalLabel.setName("totalLabel");
        JLabel countLabel = new JLabel("Кількість зобов'язань: " + derivative.getObligations().size());
        countLabel.setName("countLabel");
        JLabel avgRiskLabel = new JLabel("Середній ризик: " + derivativeService.calculateAverageRisk(derivative));
        avgRiskLabel.setName("avgRiskLabel");

        Font labelFont = DEFAULT_FONT.deriveFont(Font.BOLD);
        totalLabel.setFont(labelFont);
        countLabel.setFont(labelFont);
        avgRiskLabel.setFont(labelFont);

        summaryPanel.add(totalLabel);
        summaryPanel.add(countLabel);
        summaryPanel.add(avgRiskLabel);

        return summaryPanel;
    }

    /**
     * Створює таблицю зобов'язань та додає обробник подій для подвійного кліку.
     */
    private void createObligationsTable() {
        String[] columnNames = { "Номер полісу", "Тип", "Рівень ризику", "Сума", "Розрахована вартість", "Статус" };
        obligationsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        obligationsTable = new JTable(obligationsModel);
        obligationsTable.setName("obligationsTable");
        obligationsTable.setFont(DEFAULT_FONT);

        obligationsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && obligationsTable.getSelectedRow() != -1) {
                    int row = obligationsTable.getSelectedRow();
                    Object policyNumberObj = obligationsModel.getValueAt(row, 0);
                    InsuranceObligation selectedObligation = derivative.getObligations().stream()
                            .filter(o -> String.valueOf(o.getPolicyNumber()).equals(String.valueOf(policyNumberObj)))
                            .findFirst().orElse(null);
                    if (selectedObligation != null) {
                        ObligationDetailsDialog dialog = new ObligationDetailsDialog(
                                (JFrame) SwingUtilities.getWindowAncestor(DerivativeDetailsTab.this),
                                selectedObligation);
                        dialog.setName("obligationDetailsDialog");
                        dialog.setVisible(true);
                        if (dialog.isDataChanged()) {
                            refreshObligationsTable();
                        }
                    }
                }
            }
        });

        refreshObligationsTable();
    }

    /**
     * Оновлює дані в таблиці зобов'язань.
     */
    private void refreshObligationsTable() {
        logger.debug("Оновлення таблиці зобов'язань для деривативи: {}", derivative.getName());
        obligationsModel.setRowCount(0);
        for (InsuranceObligation obligation : derivative.getObligations()) {
            Object[] rowData = {
                    obligation.getPolicyNumber(),
                    obligation.getType(),
                    obligation.getRiskLevel(),
                    obligation.getAmount(),
                    obligation.getCalculatedValue(),
                    obligation.getStatus()
            };
            obligationsModel.addRow(rowData);
        }
    }

    /**
     * Фільтрує та сортує зобов'язання згідно з вибраними параметрами.
     */
    private void filterAndSortObligations() {
        logger.debug("Фільтрація та сортування зобов'язань для деривативи: {}", derivative.getName());
        String searchText = searchField.getText();
        String minCalcStr = minCalcValueField.getText();
        String maxCalcStr = maxCalcValueField.getText();
        String sortOption = (String) sortOptions.getSelectedItem();

        List<InsuranceObligation> filtered = insuranceService.filterAndSortObligations(derivative.getObligations(), searchText, minCalcStr, maxCalcStr, sortOption);

        obligationsModel.setRowCount(0);
        for (InsuranceObligation obligation : filtered) {
            Object[] rowData = {
                    obligation.getPolicyNumber(),
                    obligation.getType(),
                    obligation.getRiskLevel(),
                    obligation.getAmount(),
                    obligation.getCalculatedValue(),
                    obligation.getStatus()
            };
            obligationsModel.addRow(rowData);
        }
    }

    /**
     * Обробник для кнопки "Додати страхування".
     */
    private class AddButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            logger.info("Натиснуто кнопку 'Додати страхування'");
            AddObligationToDerivativeDialog dialog = new AddObligationToDerivativeDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(DerivativeDetailsTab.this),
                    derivative);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                logger.info("Додано нове страхування до деривативи: {}", derivative.getName());
                refreshObligationsTable();
                mainTabbedPane.setTitleAt(mainTabbedPane.getSelectedIndex(),
                        derivative.getName() + " (Вартість: " + derivative.getTotalValue() + ")");
            }
        }
    }

    /**
     * Обробник для кнопки "Видалити страхування".
     */
    private class DeleteButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = obligationsTable.getSelectedRow();
            if (selectedRow != -1) {
                Object policyNumberObj = obligationsModel.getValueAt(selectedRow, 0);
                InsuranceObligation toDelete = derivative.getObligations().stream()
                        .filter(o -> String.valueOf(o.getPolicyNumber()).equals(String.valueOf(policyNumberObj)))
                        .findFirst().orElse(null);

                if (toDelete != null) {
                    int confirm = JOptionPane.showConfirmDialog(
                            DerivativeDetailsTab.this,
                            "Ви впевнені, що хочете видалити це зобов'язання?",
                            "Підтвердження видалення",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            logger.info("Видалення зобов'язання з id={} з деривативи: {}", toDelete.getId(),
                                    derivative.getName());
                            new InsuranceObligationRepository().delete(toDelete.getId());
                            derivative.removeObligation(toDelete);
                            new DerivativeRepository().save(derivative);
                        } catch (Exception ex) {
                            logger.error("Не вдалося видалити зобов'язання: {}", ex.getMessage(), ex);
                            JOptionPane.showMessageDialog(DerivativeDetailsTab.this,
                                    "Не вдалося видалити зобов'язання: " + ex.getMessage(),
                                    "Помилка", JOptionPane.ERROR_MESSAGE);
                        }
                        refreshObligationsTable();
                        mainTabbedPane.setTitleAt(mainTabbedPane.getSelectedIndex(),
                                derivative.getName() + " (Вартість: " + derivative.getTotalValue() + ")");
                    }
                }
            }
        }
    }

    /**
     * Обробник для кнопки "Видалити директиву".
     */
    private class RemoveDerivativeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int confirm = JOptionPane.showConfirmDialog(
                    DerivativeDetailsTab.this,
                    "Ви впевнені, що хочете видалити цю директиву?",
                    "Підтвердження видалення директиви",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    logger.info("Видалення деривативи з id={}", derivative.getId());
                    new DerivativeRepository().delete(derivative.getId());
                } catch (Exception ex) {
                    logger.error("Не вдалося видалити директиву: {}", ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(DerivativeDetailsTab.this,
                            "Не вдалося видалити директиву: " + ex.getMessage(),
                            "Помилка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JTabbedPane tabs = mainTabbedPane;
                int idx = tabs.indexOfComponent(DerivativeDetailsTab.this);
                if (idx != -1) {
                    tabs.remove(idx);
                }
                for (int i = 0; i < tabs.getTabCount(); i++) {
                    Component comp = tabs.getComponentAt(i);
                    if (comp instanceof DerivativesTab) {
                        tabs.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
    }
}