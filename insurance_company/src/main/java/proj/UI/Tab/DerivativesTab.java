package proj.UI.Tab;

import proj.Models.Derivative;
import proj.Repositories.DerivativeRepository;
import proj.UI.Dialog.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.*;

/**
 * Вкладка для перегляду, додавання, фільтрації та сортування деривативів.
 * Дозволяє працювати з картками деривативів, відкривати деталі, додавати нові
 * деривативи,
 * а також використовувати фільтри, сортування та пагінацію.
 */
public class DerivativesTab extends AbstractTab {
    private static final Logger logger = LogManager.getLogger(DerivativesTab.class);

    private JPanel derivativesPanel;
    private JButton addButton;
    private final DerivativeRepository derivativeRepository;
    private List<Derivative> allDerivatives = new ArrayList<>();
    private JComboBox<String> sortComboBox;
    private JTextField minValueField, maxValueField;
    private JTextField searchNameField;

    private int currentPage = 1;
    private int totalPages = 1;
    private final int CARDS_PER_PAGE = 9;
    private JButton prevPageButton, nextPageButton;
    private JLabel pageLabel;

    /**
     * Створює вкладку для роботи з деривативами.
     *
     * @param mainTabbedPane головна панель вкладок
     */
    public DerivativesTab(JTabbedPane mainTabbedPane) {
        super(mainTabbedPane);
        this.derivativeRepository = new DerivativeRepository();
        initializeUI();

        mainTabbedPane.addChangeListener(e -> {
            int idx = mainTabbedPane.getSelectedIndex();
            if (idx != -1 && mainTabbedPane.getComponentAt(idx) == this) {
                loadDerivatives();
                updateDerivativesDisplay();
            }
        });
    }

    /**
     * Ініціалізує інтерфейс вкладки, панелі фільтрів, сортування, картки
     * деривативів та пагінацію.
     */
    @Override
    protected void initializeUI() {
        logger.info("Ініціалізація вкладки 'Деривативи'");
        setLayout(new BorderLayout());

        JPanel searchSortPanel = new JPanel(new GridBagLayout());
        searchSortPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;

        gbc.gridx = 0;
        addButton = createStyledButton("Додати дериватив");
        addButton.setName("addButton");
        addButton.addActionListener(new AddButtonListener());
        searchSortPanel.add(addButton, gbc);

        gbc.gridx++;
        searchSortPanel.add(new JLabel("Пошук:"), gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchNameField = createFancyTextField(12);
        searchNameField.setName("searchNameField");
        searchNameField.getDocument().addDocumentListener(new SearchDocumentListener());
        searchSortPanel.add(searchNameField, gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        searchSortPanel.add(new JLabel("Сума:"), gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        minValueField = createFancyTextField(5);
        minValueField.setName("minValueField");
        minValueField.getDocument().addDocumentListener(new SearchDocumentListener());
        searchSortPanel.add(minValueField, gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        searchSortPanel.add(new JLabel("-"), gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        maxValueField = createFancyTextField(5);
        maxValueField.setName("maxValueField");
        maxValueField.getDocument().addDocumentListener(new SearchDocumentListener());
        searchSortPanel.add(maxValueField, gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        searchSortPanel.add(new JLabel("Сортувати:"), gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        sortComboBox = new JComboBox<>(new String[] {
                "Назвою (А-Я)",
                "Назвою (Я-А)",
                "Вартістю (зростання)",
                "Вартістю (спадання)",
                "Датою (новіші)",
                "Датою (старіші)"
        });
        sortComboBox.setName("sortComboBox");
        sortComboBox.addActionListener(e -> updateDerivativesDisplay());
        searchSortPanel.add(sortComboBox, gbc);

        searchSortPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 3, true),
                "Фільтри та інструменти",
                TitledBorder.LEFT, TitledBorder.TOP,
                DEFAULT_FONT.deriveFont(Font.BOLD, 13)));

        derivativesPanel = new JPanel(new GridBagLayout());
        derivativesPanel.setName("derivativesPanel");
        derivativesPanel.setBackground(BACKGROUND_COLOR);

        derivativesPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 3, true),
                "Директиви",
                TitledBorder.LEFT, TitledBorder.TOP,
                DEFAULT_FONT.deriveFont(Font.BOLD, 13)));

        JScrollPane scrollPane = new JScrollPane(derivativesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        prevPageButton = new JButton("Назад");
        prevPageButton.setName("prevPageButton");
        nextPageButton = new JButton("Вперед");
        nextPageButton.setName("nextPageButton");
        pageLabel = new JLabel();
        pageLabel.setName("pageLabel");
        prevPageButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateDerivativesDisplay();
            }
        });
        nextPageButton.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateDerivativesDisplay();
            }
        });
        paginationPanel.add(prevPageButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextPageButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(120, 120, 180), 4, true),
                "Контроль директивами",
                TitledBorder.LEFT, TitledBorder.TOP,
                DEFAULT_FONT.deriveFont(Font.BOLD, 14)));
        mainPanel.setOpaque(false);

        mainPanel.add(searchSortPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(paginationPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        loadDerivatives();
    }

    /**
     * Завантажує всі деривативи з репозиторію та оновлює відображення.
     */
    private void loadDerivatives() {
        logger.info("Завантаження деривативів");
        derivativesPanel.removeAll();

        try {
            allDerivatives = derivativeRepository.findAll();
            updateDerivativesDisplay();
            logger.info("Деривативи успішно завантажені");
        } catch (SQLException e) {
            logger.error("Помилка під час завантаження деривативів", e);
            showErrorDialog("Не вдалося завантажити список деривативів");
        }
    }

    /**
     * Оновлює відображення карток деривативів з урахуванням фільтрів, сортування та
     * пагінації.
     */
    private void updateDerivativesDisplay() {
        derivativesPanel.removeAll();

        List<Derivative> filteredDerivatives = filterDerivatives();
        sortDerivatives(filteredDerivatives);

        int total = filteredDerivatives.size();
        totalPages = Math.max(1, (int) Math.ceil(total / (double) CARDS_PER_PAGE));
        currentPage = Math.min(currentPage, totalPages);

        int start = (currentPage - 1) * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, total);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.33;
        gbc.weighty = 0.19;

        int col = 0, row = 0;
        for (int i = start; i < end; i++) {
            JPanel card = createDerivativeCard(filteredDerivatives.get(i));
            gbc.gridx = col;
            gbc.gridy = row;
            derivativesPanel.add(card, gbc);
            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }

        pageLabel.setText("Сторінка " + currentPage + " з " + totalPages);
        prevPageButton.setEnabled(currentPage > 1);
        nextPageButton.setEnabled(currentPage < totalPages);

        derivativesPanel.revalidate();
        derivativesPanel.repaint();
    }

    /**
     * Фільтрує деривативи за назвою та діапазоном вартості.
     *
     * @return відфільтрований список деривативів
     */
    private List<Derivative> filterDerivatives() {
        List<Derivative> filtered = new ArrayList<>(allDerivatives);

        String nameSearch = searchNameField.getText().toLowerCase();
        if (!nameSearch.isEmpty()) {
            filtered.removeIf(d -> !d.getName().toLowerCase().contains(nameSearch));
        }

        try {
            double minValue = minValueField.getText().isEmpty() ? 0 : Double.parseDouble(minValueField.getText());
            double maxValue = maxValueField.getText().isEmpty() ? Double.MAX_VALUE
                    : Double.parseDouble(maxValueField.getText());

            filtered.removeIf(d -> d.getTotalValue() < minValue || d.getTotalValue() > maxValue);
        } catch (NumberFormatException e) {
            // Ігноруємо некоректні числа
        }

        return filtered;
    }

    /**
     * Сортує список деривативів згідно з вибраним критерієм.
     *
     * @param derivatives список деривативів для сортування
     */
    private void sortDerivatives(List<Derivative> derivatives) {
        String selectedSort = (String) sortComboBox.getSelectedItem();
        if (selectedSort == null)
            return;

        switch (selectedSort) {
            case "Назвою (А-Я)":
                derivatives.sort(Comparator.comparing(Derivative::getName));
                break;
            case "Назвою (Я-А)":
                derivatives.sort(Comparator.comparing(Derivative::getName).reversed());
                break;
            case "Вартістю (зростання)":
                derivatives.sort(Comparator.comparingDouble(Derivative::getTotalValue));
                break;
            case "Вартістю (спадання)":
                derivatives.sort(Comparator.comparingDouble(Derivative::getTotalValue).reversed());
                break;
            case "Датою (новіші)":
                derivatives.sort(Comparator.comparing(Derivative::getCreatedAt).reversed());
                break;
            case "Датою (старіші)":
                derivatives.sort(Comparator.comparing(Derivative::getCreatedAt));
                break;
        }
    }

    /**
     * Створює картку для відображення деривативу.
     *
     * @param derivative дериватив
     * @return панель-картка деривативу
     */
    private JPanel createDerivativeCard(Derivative derivative) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 2, true));
        card.setName("derivativeCard_" + derivative.getName());
        card.setPreferredSize(new Dimension(260, 140));
        card.setMaximumSize(new Dimension(260, 140));
        card.setMinimumSize(new Dimension(260, 140));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        JLabel nameLabel = new JLabel("Назва: " + derivative.getName());
        nameLabel.setFont(DEFAULT_FONT.deriveFont(Font.PLAIN, 14));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel dateLabel = new JLabel("Дата: " + derivative.getCreatedAt().toLocalDate().toString());
        dateLabel.setFont(DEFAULT_FONT.deriveFont(Font.PLAIN, 10));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        topPanel.add(nameLabel);
        topPanel.add(dateLabel);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);

        JLabel obligationsLabel = new JLabel("Зобов'язань: " + derivative.getObligations().size());
        obligationsLabel.setFont(DEFAULT_FONT.deriveFont(Font.PLAIN, 13));
        obligationsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel("Вартість: " + String.format("%.2f", derivative.getTotalValue()));
        valueLabel.setFont(DEFAULT_FONT.deriveFont(Font.PLAIN, 13));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottomPanel.add(obligationsLabel);
        bottomPanel.add(valueLabel);

        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        card.add(contentPanel, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            Color defaultBorder = new Color(180, 180, 180);
            Color hoverBorder = new Color(30, 60, 180);

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(hoverBorder, 2));
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(defaultBorder, 2));
                card.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    openDerivativeTab(derivative);
                }
            }
        });

        return card;
    }

    /**
     * Відкриває вкладку з деталями деривативу, якщо вона ще не відкрита.
     *
     * @param derivative дериватив
     */
    private void openDerivativeTab(Derivative derivative) {
        logger.info("Відкриття вкладки для деривативу: {}", derivative.getName());

        for (int i = 0; i < mainTabbedPane.getTabCount(); i++) {
            if (mainTabbedPane.getTitleAt(i).equals(derivative.getName())) {
                mainTabbedPane.setSelectedIndex(i);
                return;
            }
        }

        DerivativeDetailsTab detailsTab = new DerivativeDetailsTab(mainTabbedPane, derivative);
        detailsTab.setName("detailsTab_" + derivative.getName());

        JPanel tabTitlePanel = new JPanel(new BorderLayout());
        tabTitlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(derivative.getName());
        titleLabel.setFont(DEFAULT_FONT);

        JButton closeButton = new JButton("×");
        closeButton.setFont(DEFAULT_FONT.deriveFont(Font.BOLD));
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setName("detailsTab_" + derivative.getName() + "_closeButton");
        closeButton.addActionListener(e -> {
            int index = mainTabbedPane.indexOfComponent(detailsTab);
            if (index != -1) {
                mainTabbedPane.remove(index);
            }
        });

        tabTitlePanel.add(titleLabel, BorderLayout.CENTER);
        tabTitlePanel.add(closeButton, BorderLayout.EAST);

        mainTabbedPane.addTab(derivative.getName(), detailsTab);
        mainTabbedPane.setTabComponentAt(mainTabbedPane.getTabCount() - 1, tabTitlePanel);
        mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);
    }

    /**
     * Обробник для кнопки "Додати дериватив".
     */
    private class AddButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            logger.info("Натиснуто кнопку 'Додати'");
            AddEditDerivativeDialog dialog = new AddEditDerivativeDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(DerivativesTab.this),
                    null);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                try {
                    derivativeRepository.save(dialog.getDerivative());
                    logger.info("Новий дериватив збережено");
                    showSuccessDialog("Дериватив успішно додано");
                    loadDerivatives();
                } catch (SQLException ex) {
                    logger.error("Помилка під час збереження нового деривативу", ex);
                    showErrorDialog("Не вдалося зберегти дериватив");
                }
            }
        }
    }

    /**
     * Встановлює список деривативів для вкладки (наприклад, для тестування).
     *
     * @param allDerivatives список деривативів
     */
    public void setAllDerivatives(List<Derivative> allDerivatives) {
        this.allDerivatives = allDerivatives;
        updateDerivativesDisplay();
    }

    /**
     * Слухач змін у полях пошуку та фільтрів для автоматичного оновлення
     * відображення.
     */
    private class SearchDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) {
            updateDerivativesDisplay();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateDerivativesDisplay();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateDerivativesDisplay();
        }
    }
}
