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

public class DerivativesTab extends AbstractTab {
    private static final Logger logger = LogManager.getLogger(DerivativesTab.class);

    private JPanel derivativesPanel;
    private JButton addButton;
    private final DerivativeRepository derivativeRepository;
    private List<Derivative> allDerivatives = new ArrayList<>();
    private JComboBox<String> sortComboBox;
    private JTextField minValueField, maxValueField;
    private JTextField searchNameField;

    // Додайте поля для пагінації:
    private int currentPage = 1;
    private int totalPages = 1;
    private final int CARDS_PER_PAGE = 9;
    private JButton prevPageButton, nextPageButton;
    private JLabel pageLabel;

    public DerivativesTab(JTabbedPane mainTabbedPane) {
        super(mainTabbedPane);
        this.derivativeRepository = new DerivativeRepository();
        initializeUI();

        mainTabbedPane.addChangeListener(_ -> {
        int idx = mainTabbedPane.getSelectedIndex();
        if (idx != -1 && mainTabbedPane.getComponentAt(idx) == this) {
            loadDerivatives();
            updateDerivativesDisplay();
        }
    });
    }

    @Override
    protected void initializeUI() {
        logger.info("Ініціалізація вкладки 'Деривативи'");
        setLayout(new BorderLayout());

        // --- Панель інструментів і фільтрів ---
        JPanel searchSortPanel = new JPanel(new GridBagLayout());
        searchSortPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5);
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;

        // Додаємо кнопку першою
        gbc.gridx = 0;
        addButton = createStyledButton("Додати дериватив");
        addButton.addActionListener(new AddButtonListener());
        searchSortPanel.add(addButton, gbc);

        // Далі — пошук
        gbc.gridx++;
        searchSortPanel.add(new JLabel("Пошук:"), gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchNameField = createFancyTextField(12);
        searchNameField.getDocument().addDocumentListener(new SearchDocumentListener());
        searchSortPanel.add(searchNameField, gbc);

        // Сума
        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        searchSortPanel.add(new JLabel("Сума:"), gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        minValueField = createFancyTextField(5);
        minValueField.getDocument().addDocumentListener(new SearchDocumentListener());
        searchSortPanel.add(minValueField, gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        searchSortPanel.add(new JLabel("-"), gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        maxValueField = createFancyTextField(5);
        maxValueField.getDocument().addDocumentListener(new SearchDocumentListener());
        searchSortPanel.add(maxValueField, gbc);

        // Сортування
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
        sortComboBox.addActionListener(_ -> updateDerivativesDisplay());
        searchSortPanel.add(sortComboBox, gbc);

        // Додаємо обводку з назвою "Фільтри та інструменти"
        searchSortPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 3, true),
            "Фільтри та інструменти",
            TitledBorder.LEFT, TitledBorder.TOP,
            DEFAULT_FONT.deriveFont(Font.BOLD, 13)
        ));

        // --- Панель з картками деривативів ---
        derivativesPanel = new JPanel(new GridBagLayout());
        derivativesPanel.setBackground(BACKGROUND_COLOR);

        // Додаємо обводку з назвою "Директиви"
        derivativesPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180), 3, true),
            "Директиви",
            TitledBorder.LEFT, TitledBorder.TOP,
            DEFAULT_FONT.deriveFont(Font.BOLD, 13)
        ));

        JScrollPane scrollPane = new JScrollPane(derivativesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Pagination panel
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        prevPageButton = new JButton("Назад");
        nextPageButton = new JButton("Вперед");
        pageLabel = new JLabel();
        prevPageButton.addActionListener(_ -> {
            if (currentPage > 1) {
                currentPage--;
                updateDerivativesDisplay();
            }
        });
        nextPageButton.addActionListener(_ -> {
            if (currentPage < totalPages) {
                currentPage++;
                updateDerivativesDisplay();
            }
        });
        paginationPanel.add(prevPageButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextPageButton);

        // --- Головна панель вкладки ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(120, 120, 180), 4, true),
            "Контроль директивами",
            TitledBorder.LEFT, TitledBorder.TOP,
            DEFAULT_FONT.deriveFont(Font.BOLD, 14)
        ));
        mainPanel.setOpaque(false);

        mainPanel.add(searchSortPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(paginationPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);

        // Load initial data
        loadDerivatives();
    }

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

    private List<Derivative> filterDerivatives() {
        List<Derivative> filtered = new ArrayList<>(allDerivatives);

        // Filter by name
        String nameSearch = searchNameField.getText().toLowerCase();
        if (!nameSearch.isEmpty()) {
            filtered.removeIf(d -> !d.getName().toLowerCase().contains(nameSearch));
        }

        // Filter by value range
        try {
            double minValue = minValueField.getText().isEmpty() ? 0 : Double.parseDouble(minValueField.getText());
            double maxValue = maxValueField.getText().isEmpty() ? Double.MAX_VALUE
                    : Double.parseDouble(maxValueField.getText());

            filtered.removeIf(d -> d.getTotalValue() < minValue || d.getTotalValue() > maxValue);
        } catch (NumberFormatException e) {
            // Ignore invalid numbers
        }

        return filtered;
    }

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

    private JPanel createDerivativeCard(Derivative derivative) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 2, true));

        // Стандартний розмір картки (наприклад, 260x140)
        card.setPreferredSize(new Dimension(260, 140));
        card.setMaximumSize(new Dimension(260, 140));
        card.setMinimumSize(new Dimension(260, 140));

        // Контент з space-between
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10)); // знизу 0

        // Верхній блок (назва + дата)
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

        // Нижній блок (зобов'язання + вартість)
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

        // Hover effect: border стає чорно-синім
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

    private void openDerivativeTab(Derivative derivative) {
        logger.info("Відкриття вкладки для деривативу: {}", derivative.getName());

        // Check if tab already exists
        for (int i = 0; i < mainTabbedPane.getTabCount(); i++) {
            if (mainTabbedPane.getTitleAt(i).equals(derivative.getName())) {
                mainTabbedPane.setSelectedIndex(i);
                return;
            }
        }

        // Create new tab
        DerivativeDetailsTab detailsTab = new DerivativeDetailsTab(mainTabbedPane, derivative);

        // Create closeable tab
        JPanel tabTitlePanel = new JPanel(new BorderLayout());
        tabTitlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(derivative.getName());
        titleLabel.setFont(DEFAULT_FONT);

        JButton closeButton = new JButton("×");
        closeButton.setFont(DEFAULT_FONT.deriveFont(Font.BOLD));
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(_ -> {
            int index = mainTabbedPane.indexOfComponent(detailsTab);
            if (index != -1) {
                mainTabbedPane.remove(index);
            }
        });

        tabTitlePanel.add(titleLabel, BorderLayout.CENTER);
        tabTitlePanel.add(closeButton, BorderLayout.EAST);

        // Add tab
        mainTabbedPane.addTab(derivative.getName(), detailsTab);
        mainTabbedPane.setTabComponentAt(mainTabbedPane.getTabCount() - 1, tabTitlePanel);
        mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);
    }

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
