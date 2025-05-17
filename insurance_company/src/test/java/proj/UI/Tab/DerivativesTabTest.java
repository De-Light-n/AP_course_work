package proj.UI.Tab;

import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import proj.Models.Derivative;
import proj.Models.insurance.*;

import javax.swing.*;

import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DerivativesTabTest extends AssertJSwingJUnitTestCase {

    private DerivativesTab derivativesTab;
    private JTabbedPane tabbedPane;
    private FrameFixture window;

    private List<Derivative> testDerivatives;

    @Override
    protected void onSetUp() {
        // Створюємо все через EDT!
        tabbedPane = GuiActionRunner.execute(() -> {
            return new JTabbedPane();
        });

        // Створюємо вкладку через EDT
        derivativesTab = GuiActionRunner.execute(() -> new DerivativesTab(tabbedPane));

        // Створюємо тестові страхування
        HealthInsurance health = new HealthInsurance(0.5, 1000.0, 12, 30, true, 5000, true, true);
        PropertyInsurance property = new PropertyInsurance(0.3, 2000.0, 24, "Київ", 1_000_000, true, "APARTMENT", true);

        // Створюємо тестові деривативи (кожен з різними страхуваннями)
        testDerivatives = new ArrayList<>();
        Derivative dA = new Derivative("Derivative A");
        dA.addObligation(health);
        Derivative dB = new Derivative("Derivative B");
        dB.addObligation(property);
        Derivative dC = new Derivative("Derivative C");
        dC.addObligation(health);
        dC.addObligation(property);

        testDerivatives.add(dA);
        testDerivatives.add(dB);
        testDerivatives.add(dC);

        // Додаємо тестові деривативи напряму у allDerivatives через EDT
        GuiActionRunner.execute(() -> {
            derivativesTab.setAllDerivatives(testDerivatives);
            return null;
        });

        JFrame frame = GuiActionRunner.execute(() -> {
            JFrame f = new JFrame();
            f.setContentPane(derivativesTab);
            f.pack();
            f.setVisible(true);
            return f;
        });
        window = new FrameFixture(robot(), frame);
        window.show();
    }

    @Override
    protected void onTearDown() {
        if (window != null) {
            if (window.target().isShowing()) {
                window.close();
            }
            window.cleanUp();
        }
    }

    @Test
    public void testInitialization() {
        window.button("addButton").requireVisible();
        window.comboBox("sortComboBox").requireVisible();
        window.textBox("searchNameField").requireVisible();
        window.textBox("minValueField").requireVisible();
        window.textBox("maxValueField").requireVisible();
        window.panel("derivativesPanel").requireVisible();
        window.button("prevPageButton").requireVisible();
        window.button("nextPageButton").requireVisible();
        window.label("pageLabel").requireVisible();
    }

    @Test
    public void testFilterDerivativesByName() {
        window.textBox("searchNameField").setText("Derivative A");

        // Перевіряємо, що картка з ім'ям "derivativeCard_Derivative A" є видимою
        window.panel("derivativeCard_Derivative A").requireVisible();

        // Перевіряємо, що картки з іменами "derivativeCard_Derivative B" та
        // "derivativeCard_Derivative C" відсутні
        assertThrows(Exception.class, () -> window.panel("derivativeCard_Derivative B"));
        assertThrows(Exception.class, () -> window.panel("derivativeCard_Derivative C"));
    }

    @Test
    public void testFilterDerivativesByValueRange() {
        window.textBox("minValueField").setText("4000");
        window.textBox("maxValueField").setText("7000");

        // Має бути тільки "Derivative B"
        window.panel("derivativeCard_Derivative B").requireVisible();
        assertThrows(Exception.class, () -> window.panel("derivativeCard_Derivative A"));
        assertThrows(Exception.class, () -> window.panel("derivativeCard_Derivative C"));

        // Додатково: переконайтесь, що на панелі лише одна картка
        JPanel derivativesPanel = window.panel("derivativesPanel").target();
        long count = Arrays.stream(derivativesPanel.getComponents())
                .filter(c -> c instanceof JPanel && "derivativeCard_Derivative B".equals(c.getName()))
                .count();
        assertEquals(1, count);
    }

    @Test
    public void testSortDerivativesByNameAsc() {
        window.comboBox("sortComboBox").selectItem("Назвою (А-Я)");
        JPanel derivativesPanel = window.panel("derivativesPanel").target();
        // Перша картка повинна бути "Derivative A"
        JPanel firstCard = (JPanel) derivativesPanel.getComponent(0);
        assertEquals("derivativeCard_Derivative A", firstCard.getName());
    }

    @Test
    public void testSortDerivativesByNameDesc() {
        window.comboBox("sortComboBox").selectItem("Назвою (Я-А)");
        JPanel derivativesPanel = window.panel("derivativesPanel").target();
        // Перша картка повинна бути "Derivative C"
        JPanel firstCard = (JPanel) derivativesPanel.getComponent(0);
        assertEquals("derivativeCard_Derivative C", firstCard.getName());
    }

    @Test
    public void testSortDerivativesByValueAsc() {
        window.comboBox("sortComboBox").selectItem("Вартістю (зростання)");
        JPanel derivativesPanel = window.panel("derivativesPanel").target();
        // Перша картка повинна бути "Derivative A"
        JPanel firstCard = (JPanel) derivativesPanel.getComponent(0);
        assertEquals("derivativeCard_Derivative A", firstCard.getName());
    }

    @Test
    public void testSortDerivativesByValueDesc() {
        window.comboBox("sortComboBox").selectItem("Вартістю (спадання)");
        JPanel derivativesPanel = window.panel("derivativesPanel").target();
        // Перша картка повинна бути "Derivative C"
        JPanel firstCard = (JPanel) derivativesPanel.getComponent(0);
        assertEquals("derivativeCard_Derivative C", firstCard.getName());
    }

    @Test
    public void testOpenDerivativeTab() {
        // Натискаємо на картку "Derivative B"
        window.panel("derivativeCard_Derivative B").click();

        // Перевіряємо, що з'явилася вкладка з ім'ям detailsTab_Derivative B
        boolean found = false;
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component comp = tabbedPane.getComponentAt(i);
            if (comp != null && "detailsTab_Derivative B".equals(comp.getName())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Вкладка деталей Derivative B повинна бути відкрита після кліку на картку");
    }

    @Test
    public void testCloseDerivativeDetailsTab() {
        // Відкриваємо вкладку для "Derivative B"
        window.panel("derivativeCard_Derivative B").click();

        // Переконайтесь, що вкладка з'явилася
        boolean found = false;
        int tabIndex = -1;
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component comp = tabbedPane.getComponentAt(i);
            if (comp != null && "detailsTab_Derivative B".equals(comp.getName())) {
                found = true;
                tabIndex = i;
                break;
            }
        }
        assertTrue(found, "Вкладка деталей Derivative B повинна бути відкрита після кліку на картку");

        // Знаходимо панель з кнопкою закриття
        JPanel tabTitlePanel = (JPanel) tabbedPane.getTabComponentAt(tabIndex);
        JButton foundCloseButton = null;
        for (Component c : tabTitlePanel.getComponents()) {
            if (c instanceof JButton && "detailsTab_Derivative B_closeButton".equals(c.getName())) {
                foundCloseButton = (JButton) c;
                break;
            }
        }
        final JButton closeButton = foundCloseButton;
        assertNotNull(closeButton, "Кнопка закриття повинна бути присутня");

        // Натискаємо кнопку закриття через EDT
        GuiActionRunner.execute(() -> {
            closeButton.doClick();
            return null;
        });

        // Перевіряємо, що вкладка закрилася
        boolean stillFound = false;
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component comp = tabbedPane.getComponentAt(i);
            if (comp != null && "detailsTab_Derivative B".equals(comp.getName())) {
                stillFound = true;
                break;
            }
        }
        assertFalse(stillFound, "Вкладка деталей Derivative B повинна бути закрита після натискання кнопки закриття");
    }

    @Test
    public void testPagination() {
        // Додаємо 12 деривативів для перевірки пагінації (по 9 на сторінку)
        List<Derivative> manyDerivatives = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Derivative d = new Derivative("Derivative " + i);
            d.addObligation(new HealthInsurance(0.1, 1000 + i, 12, 30, false, 1000, false, false));
            manyDerivatives.add(d);
        }
        GuiActionRunner.execute(() -> derivativesTab.setAllDerivatives(manyDerivatives));

        // Перша сторінка: має бути 9 карток
        JPanel derivativesPanel = window.panel("derivativesPanel").target();
        long countPage1 = Arrays.stream(derivativesPanel.getComponents())
                .filter(c -> c instanceof JPanel && c.getName() != null && c.getName().startsWith("derivativeCard_"))
                .count();
        assertEquals(9, countPage1);

        // Натискаємо "Вперед" (наступна сторінка)
        window.button("nextPageButton").click();

        // Друга сторінка: має бути 3 картки
        derivativesPanel = window.panel("derivativesPanel").target();
        long countPage2 = Arrays.stream(derivativesPanel.getComponents())
                .filter(c -> c instanceof JPanel && c.getName() != null && c.getName().startsWith("derivativeCard_"))
                .count();
        assertEquals(3, countPage2);

        // Натискаємо "Назад" (повертаємось на першу сторінку)
        window.button("prevPageButton").click();
        derivativesPanel = window.panel("derivativesPanel").target();
        long countPage1Again = Arrays.stream(derivativesPanel.getComponents())
                .filter(c -> c instanceof JPanel && c.getName() != null && c.getName().startsWith("derivativeCard_"))
                .count();
        assertEquals(9, countPage1Again);
    }

    @Test
    public void testAddButtonOpensDialog() {
        // Натискаємо кнопку "Додати дериватив"
        window.button("addButton").click();

        // Знаходимо діалог "Додати дериватив" по заголовку
        JDialog dialog = null;
        for (Window w : Window.getWindows()) {
            if (w instanceof JDialog && w.isVisible() && "Додати дериватив".equals(((JDialog) w).getTitle())) {
                dialog = (JDialog) w;
                break;
            }
        }
        assertNotNull(dialog, "Діалог додавання деривативу повинен бути відкритий");

        // Перевіряємо наявність кнопок "Зберегти" та "Скасувати"
        JButton saveButton = null, cancelButton = null;
        for (Component c : dialog.getContentPane().getComponents()) {
            if (c instanceof JPanel) {
                for (Component btn : ((JPanel) c).getComponents()) {
                    if (btn instanceof JButton) {
                        if ("saveButton".equals(btn.getName()))
                            saveButton = (JButton) btn;
                        if ("cancelButton".equals(btn.getName()))
                            cancelButton = (JButton) btn;
                    }
                }
            }
        }
        assertNotNull(saveButton, "Кнопка 'Зберегти' повинна бути присутня у діалозі");
        assertNotNull(cancelButton, "Кнопка 'Скасувати' повинна бути присутня у діалозі");

        // Закриваємо діалог після перевірки
        dialog.dispose();
    }

}